package com.brandnewdata.mop.poc.parser;

public class ParametersParser {

    public String elementTag;

    private String nameAttribute;

    private String valueAttribute;

    private String labelAttribute;

    private String typeAttribute;

    private String innerTypeAttribute;

    public ParametersParser(String elementTag, String nameAttribute, String valueAttribute, String labelAttribute, String typeAttribute, String innerTypeAttribute) {
        this.elementTag = elementTag;
        this.nameAttribute = nameAttribute;
        this.valueAttribute = valueAttribute;
        this.labelAttribute = labelAttribute;
        this.typeAttribute = typeAttribute;
        this.innerTypeAttribute = innerTypeAttribute;
    }

    


}
