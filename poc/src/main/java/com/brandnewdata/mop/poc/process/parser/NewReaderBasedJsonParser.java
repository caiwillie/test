package com.brandnewdata.mop.poc.process.parser;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;

@Slf4j
public class NewReaderBasedJsonParser extends ReaderBasedJsonParser {
    public NewReaderBasedJsonParser(IOContext ctxt, int features, Reader r, ObjectCodec codec, CharsToNameCanonicalizer st, char[] inputBuffer, int start, int end, boolean bufferRecyclable) {
        super(ctxt, features, r, codec, st, inputBuffer, start, end, bufferRecyclable);
    }

    public NewReaderBasedJsonParser(IOContext ctxt, int features, Reader r, ObjectCodec codec, CharsToNameCanonicalizer st) {
        super(ctxt, features, r, codec, st);
    }

    @Override
    protected JsonToken _handleOddValue(int i) throws IOException {
        try {
            return super._handleOddValue(i);
        } catch (Exception e) {
            if(e.getMessage().startsWith("Unrecognized token")) {
                // token 异常，当作 null 处理
                return _parseJavaIdentifier(i);
            } else {
                // 其他异常，直接抛出
                throw e;
            }
        }
    }

    protected final JsonToken _parseJavaIdentifier(int ch) throws IOException
    {

        int startPtr = _inputPtr-1; // to include digit already read

        // _inputPtr 指示下一个位置
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            char c = _inputBuffer[_inputPtr];
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
        }

        return JsonToken.VALUE_NULL;
    }
}
