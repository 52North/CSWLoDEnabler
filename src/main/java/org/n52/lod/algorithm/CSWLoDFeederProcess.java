package org.n52.lod.algorithm;

import java.io.IOException;

import org.n52.lod.csw.CSWLoDEnabler;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(
        title = "OGC CSW to LOD Triple Store Feeder", version = "0.1")
public class CSWLoDFeederProcess extends CSWLoDEnabler {

    private static final Logger log = LoggerFactory.getLogger(CSWLoDFeederProcess.class);

    public CSWLoDFeederProcess(boolean addToTripleStore, boolean saveToFile) throws IOException {
        super(addToTripleStore, saveToFile);
        log.info("NEW {}", this);
    }

    public CSWLoDFeederProcess() throws IOException {
        super(false, false);
    }

    @LiteralDataInput(
            identifier = "addToTripleStore", title = "Insert to triple store", binding = LiteralBooleanBinding.class, abstrakt = "The harvested data is inserted into a pre-configured triplestore",
            defaultValue = "true", minOccurs = 0, maxOccurs = 1)
    public boolean addToTripleStoreInput;

    @LiteralDataInput(
            identifier = "saveToTempfile", title = "Save to tempfile", binding = LiteralBooleanBinding.class, abstrakt = "The data is stored in two temporary files for TURTLE and RDF/XML format",
            defaultValue = "true", minOccurs = 0, maxOccurs = 1)
    public boolean saveToTempfileInput;

    @LiteralDataOutput(
            identifier = "report", title = "Transfer report", binding = LiteralStringBinding.class, abstrakt = "The information collected during the harvesting and inserting as a human readable text")
    public String reportString;

    @Execute
    @Override
    public void runOverAll() throws IOException {
        this.addToTripleStore = addToTripleStoreInput;
        this.saveToFile = saveToTempfileInput;

        log.info("Running {}", this);
        super.runOverAll();

        this.reportString = report.extendedToString();

        log.info("Algorihtm executed.");
    }

}
