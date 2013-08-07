package org.n52.lod;

import java.io.File;

import junit.framework.TestCase;

import org.n52.util.IoUtils;

public class CatalogInteractorTest extends TestCase {

    public void testExecuteGetRecords()
    {
        try {
            String result = new CatalogInteractor().executeGetRecords(10, 2000);
            
            IoUtils.saveFile(new File("c:/temp/catalogResult_GetRecords.xml"), result);
            
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();

            fail();
        }
    }

    public void testExecuteGetRecordsById()
    {
        try {
            String result = new CatalogInteractor().executeGetRecordsById(Constants.getInstance().getTestRecordId());
            
            IoUtils.saveFile(new File("c:/temp/catalogResult_GetRecordsById.xml"), result);
            
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();

            fail();
        }
    }

}
