package org.n52.lod.csw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.oxf.util.web.HttpClientException;

public class CatalogInteractorTestManual {

    private static Path tempdir;

    @BeforeClass
    public static void tempdir() throws IOException {
        tempdir = Files.createTempDirectory("csw2lod_");
    }

    private CatalogInteractor interactor;

    @Before
    public void createInteractor() throws IOException {
        interactor = new CatalogInteractor();
    }

    @Test
    public void testExecuteGetRecords() throws Exception {
        String result = interactor.executeGetRecords(10, 2000);

        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecords.xml"));

        Files.write(file, result.getBytes());

        System.out.println("Saved file to " + file);
    }

    @Test
    public void testExecuteGetRecordsById() throws IOException, Exception {
        String result = interactor.executeGetRecordsById(Constants.getInstance().getTestRecordId());

        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecordsById.xml"));
        Files.write(file, result.getBytes());

        System.out.println("Saved file to " + file);
    }

    @Test
    public void countRecordsInCSW() throws IllegalStateException, HttpClientException, IOException, XmlException {
        long numberOfRecords = interactor.getNumberOfRecords();

        System.out.println(numberOfRecords);
    }

}
