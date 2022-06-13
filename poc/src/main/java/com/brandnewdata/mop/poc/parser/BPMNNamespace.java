package com.brandnewdata.mop.poc.parser;

/**
 * The enum Bpmn namespace enum.
 *
 * @author caiwillie  The enum Bpmn namespace enmu.
 */
public enum BPMNNamespace {

    /*
     * xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
     * xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"
     * xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
     * xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
     * xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *
     *

     * xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
     * xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
     * xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
     * xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
     * xmlns:ra="https://www.brandnewdata.com"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * */


    /**
     * Bpmn bpmn namespace enmu.
     */
    BPMN("bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL"),


    /**
     * Zeebe bpmn namespace enum.
     */
    ZEEBE("zeebe", "http://camunda.org/schema/zeebe/1.0"),

    BPMN2("bpmn2", "http://www.omg.org/spec/BPMN/20100524/MODEL"),

    BRANDNEWDATA("brandnewdata", "https://www.brandnewdata.com");

    /**
     * The prefix.
     */
    private String prefix;

    /**
     * The Url.
     */
    private String uri;

    /**
     * Instantiates a new Bpmn namespace enmu.
     *
     * @param prefix the name
     * @param uri    the url
     */
    BPMNNamespace(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUri() {
        return uri;
    }


}
