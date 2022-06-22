/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.auth0.jwt.exceptions.JWTDecodeException
 *  com.auth0.jwt.impl.JWTParser
 *  com.auth0.jwt.interfaces.Claim
 *  com.auth0.jwt.interfaces.DecodedJWT
 *  com.auth0.jwt.interfaces.Header
 *  com.auth0.jwt.interfaces.Payload
 *  io.camunda.operate.webapp.security.util.JWTDecoder$TokenUtils
 */
package io.camunda.operate.webapp.security.util;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Header;
import com.auth0.jwt.interfaces.Payload;
import io.camunda.operate.webapp.security.util.JWTDecoder;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Exception performing whole class analysis ignored.
 */
public class JWTDecoder
implements DecodedJWT,
Serializable {
    private static final long serialVersionUID = 1873362438023312895L;
    private final String[] parts;
    private final Header header;
    private final Payload payload;

    public JWTDecoder(String jwt) throws JWTDecodeException {
        this(new JWTParser(), jwt);
    }

    public JWTDecoder(JWTParser converter, String jwt) throws JWTDecodeException {
        String payloadJson;
        String headerJson;
        this.parts = TokenUtils.splitToken((String)jwt);
        try {
            headerJson = new String(Base64.getUrlDecoder().decode(this.parts[0]), StandardCharsets.UTF_8);
            payloadJson = new String(Base64.getUrlDecoder().decode(this.parts[1]), StandardCharsets.UTF_8);
        }
        catch (NullPointerException var6) {
            throw new JWTDecodeException("The UTF-8 Charset isn't initialized.", (Throwable)var6);
        }
        catch (IllegalArgumentException var7) {
            throw new JWTDecodeException("The input is not a valid base 64 encoded string.", (Throwable)var7);
        }
        this.header = converter.parseHeader(headerJson);
        this.payload = converter.parsePayload(payloadJson);
    }

    public Payload getPayloadObject() {
        return this.payload;
    }

    public String getAlgorithm() {
        return this.header.getAlgorithm();
    }

    public String getType() {
        return this.header.getType();
    }

    public String getContentType() {
        return this.header.getContentType();
    }

    public String getKeyId() {
        return this.header.getKeyId();
    }

    public Claim getHeaderClaim(String name) {
        return this.header.getHeaderClaim(name);
    }

    public String getIssuer() {
        return this.payload.getIssuer();
    }

    public String getSubject() {
        return this.payload.getSubject();
    }

    public List<String> getAudience() {
        return this.payload.getAudience();
    }

    public Date getExpiresAt() {
        return this.payload.getExpiresAt();
    }

    public Date getNotBefore() {
        return this.payload.getNotBefore();
    }

    public Date getIssuedAt() {
        return this.payload.getIssuedAt();
    }

    public String getId() {
        return this.payload.getId();
    }

    public Claim getClaim(String name) {
        return this.payload.getClaim(name);
    }

    public Map<String, Claim> getClaims() {
        return this.payload.getClaims();
    }

    public String getHeader() {
        return this.parts[0];
    }

    public String getPayload() {
        return this.parts[1];
    }

    public String getSignature() {
        return this.parts[2];
    }

    public String getToken() {
        return String.format("%s.%s.%s", this.parts[0], this.parts[1], this.parts[2]);
    }

    static abstract class TokenUtils {
        TokenUtils() {
        }

        static String[] splitToken(String token) throws JWTDecodeException {
            String[] parts = token.split("\\.");
            if (parts.length == 2 && token.endsWith(".")) {
                parts = new String[]{parts[0], parts[1], ""};
            }
            if (parts.length == 3) return parts;
            throw new JWTDecodeException(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
        }
    }
}
