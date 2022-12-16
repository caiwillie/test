package com.brandnewdata.mop.poc.proxy.servlet;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.annotation.Nullable;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class RepeatReadHttpRequest extends HttpServletRequestWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatReadHttpRequest.class);
    private final ByteArrayOutputStream cachedContent;
    private Map<String, String[]> cachedForm;

    @Nullable
    private ServletInputStream inputStream;

    public RepeatReadHttpRequest(HttpServletRequest request) {
        super(request);
        this.cachedContent = new ByteArrayOutputStream();
        this.cachedForm = new HashMap<>();
        cacheData();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        this.inputStream = new RepeatReadInputStream(cachedContent.toByteArray());
        return this.inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return (enc != null ? enc : WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public String getParameter(String name) {
        String value = null;
        if (isFormPost()) {
            String[] values = cachedForm.get(name);
            if (null != values && values.length > 0) {
                value = values[0];
            }
        }

        if (StringUtils.isEmpty(value)) {
            value = super.getParameter(name);
        }

        return value;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return cachedForm;
        }

        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return Collections.enumeration(cachedForm.keySet());
        }

        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return cachedForm.get(name);
        }

        return super.getParameterValues(name);
    }

    private void cacheData() {
        try {
            if (isFormPost()) {
                this.cachedForm = super.getParameterMap();
            } else {
                ServletInputStream inputStream = super.getInputStream();
                IOUtils.copy(inputStream, this.cachedContent);
            }
        } catch (IOException e) {
            LOGGER.warn("[RepeatReadHttpRequest:cacheData], error: {}", e.getMessage());
        }

    }

    private boolean isFormPost() {
        String contentType = getContentType();
        return (contentType != null &&
                (contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) ||
                        contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) &&
                HttpMethod.POST.matches(getMethod()));
    }

    private static class RepeatReadInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public RepeatReadInputStream(byte[] bytes) {
            this.inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }

        @Override
        public int readLine(byte[] b, int off, int len) throws IOException {
            return this.inputStream.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return this.inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }
    }

}
