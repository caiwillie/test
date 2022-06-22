/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonProcessingException
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.property.OperateProperties
 *  io.camunda.operate.webapp.security.OperateProfileService
 *  javax.servlet.ServletContext
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.property.OperateProperties;
import io.camunda.operate.webapp.security.OperateProfileService;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientConfig {
    @Autowired
    private OperateProfileService profileService;
    @Autowired
    private OperateProperties operateProperties;
    @Autowired
    private ServletContext context;
    public boolean isEnterprise;
    public boolean canLogout;
    public String contextPath;
    public String organizationId;
    public String clusterId;
    public String mixpanelAPIHost;
    public String mixpanelToken;
    public boolean isLoginDelegated;

    public String asJson() {
        this.isEnterprise = this.operateProperties.isEnterprise();
        this.clusterId = this.operateProperties.getCloud().getClusterId();
        this.organizationId = this.operateProperties.getCloud().getOrganizationId();
        this.mixpanelAPIHost = this.operateProperties.getCloud().getMixpanelAPIHost();
        this.mixpanelToken = this.operateProperties.getCloud().getMixpanelToken();
        this.contextPath = this.context.getContextPath();
        this.canLogout = this.profileService.currentProfileCanLogout();
        this.isLoginDelegated = this.profileService.isLoginDelegated();
        try {
            return String.format("window.clientConfig = %s;", new ObjectMapper().writeValueAsString((Object)this));
        }
        catch (JsonProcessingException e) {
            return "window.clientConfig = {};";
        }
    }
}
