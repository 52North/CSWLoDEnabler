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
package org.n52.lod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class Report {

    public int added = 0;

    public long startIndex = 0;

    public long recordNumber = 0;

    public List<String> addedIds = Lists.newArrayList();

    public Map<String, Object> retrievalIssues = Maps.newHashMap();

    public Map<String, Object> issues = Maps.newHashMap();

    public Report() {
        //
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Report [added=");
        builder.append(added);
        builder.append(", recordNumber=");
        builder.append(recordNumber);
        builder.append(", startIndex=");
        builder.append(startIndex);
        if (addedIds != null) {
            builder.append(", # added=");
            builder.append(addedIds.size());
        }
        if (issues != null) {
            builder.append(", # issues=");
            builder.append(issues.size());
        }
        if (issues != null) {
            builder.append(", # retrieval issue=");
            builder.append(retrievalIssues.size());
        }
        builder.append("]");
        return builder.toString();
    }

    public String extendedToString() {
        StringBuilder builder = new StringBuilder();
        builder.append(toString());
        if (addedIds != null) {
            builder.append("\n\n\n\n\n********************** Added **********************\n");
            builder.append(Arrays.toString(addedIds.toArray()));
            builder.append(", ");
        }
        if (!issues.isEmpty()) {
            builder.append("\n\n\n\n\n********************** Issues **********************\n");
            builder.append(Joiner.on("\n\n").withKeyValueSeparator(" : ").join(issues));
        }
        if (!retrievalIssues.isEmpty()) {
            builder.append("\n\n\n\n\n***************** Retrieval Issues *****************\n");
            builder.append(Joiner.on("\n\n").withKeyValueSeparator(" : ").join(retrievalIssues));
        }
        builder.append("\n\n");
        builder.append(toString());
        return builder.toString();
    }
}