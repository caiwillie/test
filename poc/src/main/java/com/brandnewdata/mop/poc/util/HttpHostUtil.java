package com.brandnewdata.mop.poc.util;

import org.apache.http.HttpHost;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpHostUtil {

    public static HttpHost createHttpHost(String uri) {
        try {
            return createHttpHost(URI.create(uri));
        }
        catch (IllegalArgumentException ex) {
            return HttpHost.create(uri);
        }
    }

    public static HttpHost createHttpHost(URI uri) {
        if (!StringUtils.hasLength(uri.getUserInfo())) {
            return HttpHost.create(uri.toString());
        }
        try {
            return HttpHost.create(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment()).toString());
        }
        catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
