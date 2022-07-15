package com.brandnewdata.mop.poc.parser;

import com.brandnewdata.connector.api.IConnectorConfFeign;

public interface XMLParseStep1 {

    XMLParseStep2 parse(String xml);

    interface XMLParseStep2 {

        XMLParseStep1.XMLParseStep3 replaceGeneralTrigger();

        XMLParseStep1.XMLParseStep3 replaceCustomTrigger();

        XMLParseStep1.XMLParseStep4 replaceProperties(IConnectorConfFeign client);

        XMLDTO build();
    }

    interface XMLParseStep3 {

        XMLParseStep1.XMLParseStep4 replaceProperties(IConnectorConfFeign client);

        XMLDTO build();
    }

    interface XMLParseStep4 {

        XMLDTO build();
    }
}
