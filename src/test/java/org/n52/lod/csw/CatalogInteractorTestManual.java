package org.n52.lod.csw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.util.IoUtils;

public class CatalogInteractorTestManual {
    
    private static Path tempdir;

    @BeforeClass
    public static void tempdir() throws IOException {
         tempdir = Files.createTempDirectory("csw2lod_");
    }
    
    @Test
    public void testExecuteGetRecords() throws Exception {
        String result = new CatalogInteractor().executeGetRecords(10, 2000);
        
        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecords.xml"));
        
        IoUtils.saveFile(file.toFile(), result);

        System.out.println("Saved file to " + file);
    }

    @Test
    public void testExecuteGetRecordsById() throws IOException, Exception {
        String result = new CatalogInteractor().executeGetRecordsById(Constants.getInstance().getTestRecordId());

        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecordsById.xml"));
        IoUtils.saveFile(file.toFile(), result);

        System.out.println("Saved file to " + file);
    }

}
