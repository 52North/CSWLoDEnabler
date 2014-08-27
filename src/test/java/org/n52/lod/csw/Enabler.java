package org.n52.lod.csw;

import java.io.IOException;

import org.junit.Test;

public class Enabler {
    
    @Test
    public void saveToFile() throws IOException {
        CSWLoDEnabler enabler = new CSWLoDEnabler(false, true);
        
        enabler.runOverAll();
    }

}
