package com.brandnewdata.mop.poc.process.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import java.io.IOException;

public class FeelVariablesJsonFactory extends MappingJsonFactory {

    @Override
    protected JsonParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recyclable) throws IOException {
        return new NewReaderBasedJsonParser(ctxt, _parserFeatures, null, _objectCodec,
                _rootCharSymbols.makeChild(_factoryFeatures),
                data, offset, offset+len, recyclable);
    }
}
