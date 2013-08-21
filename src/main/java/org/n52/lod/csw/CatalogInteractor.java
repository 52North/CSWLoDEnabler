package org.n52.lod.csw;


import net.opengis.cat.csw.x202.ElementSetNameType;
import net.opengis.cat.csw.x202.ResultType;

import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.csw.adapter.CSWAdapter;
import org.n52.oxf.csw.adapter.CSWRequestBuilder;
import org.n52.oxf.ows.capabilities.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogInteractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogInteractor.class);
    
    public String executeGetRecords(int maxRecords, int startPos) throws Exception {
        
        CSWAdapter adapter = new CSWAdapter();
        
        ParameterContainer paramCon = new ParameterContainer();
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_MAX_RECORDS, maxRecords);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_START_POSITION, startPos);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_RESULT_TYPE, ResultType.RESULTS.toString());
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_OUTPUT_FORMAT_PARAMETER, "application/xml");
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_OUTPUT_SCHEMA_FORMAT, "http://www.opengis.net/cat/csw/2.0.2" );
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_QUERY_TYPE_NAMES_PARAMETER, "csw:Record");
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORDS_ELEMENT_SET_NAME_FORMAT, ElementSetNameType.BRIEF.toString());
        
        OperationResult opResult = adapter.doOperation(new Operation(CSWAdapter.GET_RECORDS,
                Constants.getInstance().getUrlCSW() + "?",
                Constants.getInstance().getUrlCSW()), paramCon);

        String result = new String(opResult.getIncomingResult());
        
        return result;
    }
    
    public String executeGetRecordsById(String recordID) throws Exception
    {
        LOGGER.info("Calling GetRecordsById for record '" + recordID + "'");
        
        CSWAdapter adapter = new CSWAdapter();

        String elementSetName = "full";
        String outputSchema = Constants.getInstance().getNsGMD();
        // String outputSchema = Constants.NS_CSW;

        ParameterContainer paramCon = new ParameterContainer();
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_REQUEST, CSWAdapter.GET_RECORD_BY_ID);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_VERSION, CSWAdapter.SUPPORTED_VERSIONS[0]);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_SERVICE, CSWAdapter.SERVICE_TYPE);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_ID, recordID);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_ELEMENT_SET_NAME, elementSetName);
        paramCon.addParameterShell(CSWRequestBuilder.GET_RECORD_BY_ID_OUTPUT_SCHEMA, outputSchema);

        OperationResult opResult = adapter.doOperation(new Operation(CSWAdapter.GET_RECORD_BY_ID, Constants.getInstance().getUrlCSW() + "?", Constants.getInstance().getUrlCSW()), paramCon);
        
        String result = new String(opResult.getIncomingResult());

        return result;
    }
    
}
