/**
 * ﻿Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.lod.triplestore;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.xmlbeans.XmlObject;
import org.n52.lod.Report;
import org.n52.lod.csw.mapping.XmlToRdfMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hp.hpl.jena.rdf.model.Model;

public abstract class AbstractWorkerTripleSink extends AbstractTripleSink {

    protected static final Logger log = LoggerFactory.getLogger(AbstractWorkerTripleSink.class);

    private static final int NUM_THREADS = 4;

    public AbstractWorkerTripleSink(XmlToRdfMapper mapper) {
        super(mapper);
        log.info("NEW {} with {} sink feed threads", this, NUM_THREADS);
    }

    private class CallableMapper implements Callable<Model> {

        private XmlObject xml;

        private XmlToRdfMapper mapper;

        public CallableMapper(XmlToRdfMapper mapper, XmlObject xml) {
            this.xml = xml;
            this.mapper = mapper;
        }

        @Override
        public Model call() throws Exception {
            return this.mapper.map(xml);
        }

    }

    @Override
    protected int addRecordsToModel(Map<String, GetRecordByIdResponseDocument> records,
            final Model m,
            final Report report) {
        final MutableInt counter = new MutableInt(0);
        final long modelSizeBefore = m.size();
        
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(NUM_THREADS));

        for (Entry<String, GetRecordByIdResponseDocument> entry : records.entrySet()) {
            
            final String id = entry.getKey();
            log.debug("Adding {} to the model", id);

            CallableMapper c = new CallableMapper(this.mapper.replicate(), entry.getValue());
            ListenableFuture<Model> modelFuture = executorService.submit(c);

            Futures.addCallback(modelFuture, new FutureCallback<Model>() {

                @Override
                public void onFailure(Throwable t) {
                    log.error("Error mapping xml to model", t);
                    report.issues.put(id, "Error while adding to model: " + t.getMessage());
                }

                @Override
                public void onSuccess(Model result) {
                    log.trace("Adding result to model: {}", result);
                    m.add(result);
                    log.trace("ADDED result to mode.");

                    counter.increment();
                    report.added++;
                    report.addedIds.add(id);
                }

            });
        }
        
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                log.error("Could not await termination", e);
            }
        }
        
        log.debug("Added {} of {} records to model {}, which now has size {} (before {})", counter, records.size(), m.getClass(), m.size(), modelSizeBefore);
        return counter.intValue();
    }
}
