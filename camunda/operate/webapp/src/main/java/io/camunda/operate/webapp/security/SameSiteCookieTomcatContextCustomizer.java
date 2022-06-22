/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.catalina.Context
 *  org.apache.tomcat.util.http.CookieProcessor
 *  org.apache.tomcat.util.http.Rfc6265CookieProcessor
 *  org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.stereotype.Component;

@Component
public class SameSiteCookieTomcatContextCustomizer
implements TomcatContextCustomizer {
    public void customize(Context context) {
        Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
        cookieProcessor.setSameSiteCookies("Lax");
        context.setCookieProcessor((CookieProcessor)cookieProcessor);
    }
}
