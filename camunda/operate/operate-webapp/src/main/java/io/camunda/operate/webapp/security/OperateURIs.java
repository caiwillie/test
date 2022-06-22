/*
 * Decompiled with CFR 0.152.
 */
package io.camunda.operate.webapp.security;

public final class OperateURIs {
    public static final String RESPONSE_CHARACTER_ENCODING = "UTF-8";
    public static final String ROOT = "/";
    public static final String API = "/api/**";
    public static final String PUBLIC_API = "/v*/**";
    public static final String LOGIN_RESOURCE = "/api/login";
    public static final String LOGOUT_RESOURCE = "/api/logout";
    public static final String COOKIE_JSESSIONID = "OPERATE-SESSION";
    public static final String SSO_CALLBACK_URI = "/sso-callback";
    public static final String NO_PERMISSION = "/noPermission";
    public static final String IDENTITY_CALLBACK_URI = "/identity-callback";
    public static final String REQUESTED_URL = "requestedUrl";
    public static final String[] AUTH_WHITELIST = new String[]{"/swagger-resources", "/swagger-resources/**", "/swagger-ui.html", "/documentation", "/api/login", "/api/logout"};

    private OperateURIs() {
    }
}
