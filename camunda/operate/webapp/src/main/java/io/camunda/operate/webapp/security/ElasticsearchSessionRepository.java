/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.es.RetryElasticsearchClient
 *  io.camunda.operate.schema.indices.OperateWebSessionIndex
 *  io.camunda.operate.webapp.security.ElasticsearchSessionRepository$ElasticsearchSession
 *  javax.annotation.PostConstruct
 *  javax.annotation.PreDestroy
 *  org.elasticsearch.action.search.SearchRequest
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.core.convert.support.GenericConversionService
 *  org.springframework.core.serializer.support.DeserializingConverter
 *  org.springframework.core.serializer.support.SerializingConverter
 *  org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
 *  org.springframework.session.SessionRepository
 *  org.springframework.session.config.annotation.web.http.EnableSpringHttpSession
 *  org.springframework.session.web.http.CookieSerializer
 *  org.springframework.session.web.http.DefaultCookieSerializer
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security;

import io.camunda.operate.es.RetryElasticsearchClient;
import io.camunda.operate.schema.indices.OperateWebSessionIndex;
import io.camunda.operate.webapp.security.ElasticsearchSessionRepository;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.elasticsearch.action.search.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.stereotype.Component;

@Configuration
@ConditionalOnExpression(value="${camunda.operate.persistent.sessions.enabled:false} or ${camunda.operate.persistentSessionsEnabled:false}")
@Component
@EnableSpringHttpSession
public class ElasticsearchSessionRepository implements SessionRepository<ElasticsearchSessionRepository.ElasticsearchSession> {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSessionRepository.class);
    public static final int DELETE_EXPIRED_SESSIONS_DELAY = 1800000;
    @Autowired
    private RetryElasticsearchClient retryElasticsearchClient;
    @Autowired
    private GenericConversionService conversionService;
    @Autowired
    private OperateWebSessionIndex operateWebSessionIndex;
    @Autowired
    @Qualifier(value="sessionThreadPoolScheduler")
    private ThreadPoolTaskScheduler sessionThreadScheduler;

    @PostConstruct
    private void setUp() {
        logger.debug("Persistent sessions in Elasticsearch enabled");
        this.setupConverter();
        this.startExpiredSessionCheck();
    }

    @PreDestroy
    private void tearDown() {
        logger.debug("Shutdown ElasticsearchSessionRepository");
    }

    @Bean(value={"sessionThreadPoolScheduler"})
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(5);
        executor.setThreadNamePrefix("operate_session_");
        executor.initialize();
        return executor;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("OPERATE-SESSION");
        return serializer;
    }

    private void setupConverter() {
        this.conversionService.addConverter(Object.class, byte[].class, (Converter)new SerializingConverter());
        this.conversionService.addConverter(byte[].class, Object.class, (Converter)new DeserializingConverter());
    }

    private void startExpiredSessionCheck() {
        this.sessionThreadScheduler.scheduleAtFixedRate(this::removedExpiredSessions, 1800000L);
    }

    private void removedExpiredSessions() {
        logger.debug("Check for expired sessions");
        SearchRequest searchRequest = new SearchRequest(new String[]{this.operateWebSessionIndex.getFullQualifiedName()});
        this.retryElasticsearchClient.doWithEachSearchResult(searchRequest, sh -> {
            Map document = sh.getSourceAsMap();
            Optional<ElasticsearchSession> maybeSession = this.documentToSession(document);
            if (maybeSession.isPresent()) {
                ElasticsearchSession session = maybeSession.get();
                logger.debug("Check if session {} is expired: {}", (Object)session, (Object)session.isExpired());
                if (!session.isExpired()) return;
                this.delete(session.getId());
            } else {
                this.delete(this.getSessionIdFrom(document));
            }
        });
    }

    public ElasticsearchSession createSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        ElasticsearchSession session = new ElasticsearchSession(sessionId);
        logger.debug("Create session {} with maxInactiveTime {} s", (Object)session, (Object)session.getMaxInactiveIntervalInSeconds());
        return session;
    }

    public void save(ElasticsearchSession session) {
        logger.debug("Save session {}", (Object)session);
        if (session.isExpired()) {
            this.delete(session.getId());
            return;
        }
        if (!session.isChanged()) return;
        logger.debug("Session {} changed, save in Elasticsearch.", (Object)session);
        this.retryElasticsearchClient.createOrUpdateDocument(this.operateWebSessionIndex.getFullQualifiedName(), session.getId(), this.sessionToDocument(session));
        session.clearChangeFlag();
    }

    public ElasticsearchSession getSession(String id) {
        logger.debug("Retrieve session {} from Elasticsearch", (Object)id);
        this.retryElasticsearchClient.refresh(this.operateWebSessionIndex.getFullQualifiedName());
        Map document = this.retryElasticsearchClient.getDocument(this.operateWebSessionIndex.getFullQualifiedName(), id);
        if (document == null) {
            return null;
        }
        Optional<ElasticsearchSession> maybeSession = this.documentToSession(document);
        if (maybeSession.isEmpty()) {
            this.delete(this.getSessionIdFrom(document));
            return null;
        }
        ElasticsearchSession session = maybeSession.get();
        if (!session.isExpired()) return session;
        this.delete(session.getId());
        return null;
    }

    public void delete(String id) {
        logger.debug("Delete session {}", (Object)id);
        this.executeAsyncElasticsearchRequest(() -> this.retryElasticsearchClient.deleteDocument(this.operateWebSessionIndex.getFullQualifiedName(), id));
    }

    private byte[] serialize(Object object) {
        return (byte[])this.conversionService.convert(object, TypeDescriptor.valueOf(Object.class), TypeDescriptor.valueOf(byte[].class));
    }

    private Object deserialize(byte[] bytes) {
        return this.conversionService.convert((Object)bytes, TypeDescriptor.valueOf(byte[].class), TypeDescriptor.valueOf(Object.class));
    }

    private Map<String, Object> sessionToDocument(ElasticsearchSession session) {
        HashMap attributes = new HashMap();
        session.getAttributeNames().forEach(name -> attributes.put(name, this.serialize(session.getAttribute(name))));
        return Map.of("id", session.getId(), "creationTime", session.getCreationTime(), "lastAccessedTime", session.getLastAccessedTime(), "maxInactiveIntervalInSeconds", session.getMaxInactiveIntervalInSeconds(), "attributes", attributes);
    }

    private String getSessionIdFrom(Map<String, Object> document) {
        return (String)document.get("id");
    }

    private Optional<ElasticsearchSession> documentToSession(Map<String, Object> document) {
        try {
            String sessionId = this.getSessionIdFrom(document);
            ElasticsearchSession session = new ElasticsearchSession(sessionId);
            session.setCreationTime(((Long)document.get("creationTime")).longValue());
            session.setLastAccessedTime(((Long)document.get("lastAccessedTime")).longValue());
            session.setMaxInactiveIntervalInSeconds(((Integer)document.get("maxInactiveIntervalInSeconds")).intValue());
            Object attributesObject = document.get("attributes");
            if (attributesObject == null) return Optional.of(session);
            if (!attributesObject.getClass().isInstance(new HashMap())) return Optional.of(session);
            Map<String, Object> attributes = (Map)document.get("attributes");
            attributes.keySet().forEach(name -> session.setAttribute(name, this.deserialize(Base64.getDecoder().decode((String)attributes.get(name)))));
            return Optional.of(session);
        }
        catch (Exception e) {
            logger.error("Could not restore session.", e);
            return Optional.empty();
        }
    }

    private void executeAsyncElasticsearchRequest(Runnable requestRunnable) {
        this.sessionThreadScheduler.execute(requestRunnable);
    }

    public static class ElasticsearchSession implements ExpiringSession {
        private final MapSession delegate;
        private boolean changed;

        public ElasticsearchSession(String id) {
            this.delegate = new MapSession(id);
        }

        boolean isChanged() {
            return this.changed;
        }

        void clearChangeFlag() {
            this.changed = false;
        }

        public String getId() {
            return this.delegate.getId();
        }

        public ElasticsearchSession setId(String id) {
            this.delegate.setId(id);
            return this;
        }

        public <T> T getAttribute(String attributeName) {
            return (T)this.delegate.getAttribute(attributeName);
        }

        public Set<String> getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public void setAttribute(String attributeName, Object attributeValue) {
            this.delegate.setAttribute(attributeName, attributeValue);
            this.changed = true;
        }

        public void removeAttribute(String attributeName) {
            this.delegate.removeAttribute(attributeName);
            this.changed = true;
        }

        public long getCreationTime() {
            return this.delegate.getCreationTime();
        }

        public void setCreationTime(long creationTime) {
            this.delegate.setCreationTime(creationTime);
            this.changed = true;
        }

        public void setLastAccessedTime(long lastAccessedTime) {
            this.delegate.setLastAccessedTime(lastAccessedTime);
            this.changed = true;
        }

        public long getLastAccessedTime() {
            return this.delegate.getLastAccessedTime();
        }

        public void setMaxInactiveIntervalInSeconds(int interval) {
            this.delegate.setMaxInactiveIntervalInSeconds(interval);
            this.changed = true;
        }

        public int getMaxInactiveIntervalInSeconds() {
            return this.delegate.getMaxInactiveIntervalInSeconds();
        }

        public boolean isExpired() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return this.delegate.isExpired() || authentication != null && !authentication.isAuthenticated();
        }

        public String toString() {
            return String.format("ElasticsearchSession: %s ", this.getId());
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) return false;
            if (this.getClass() != o.getClass()) {
                return false;
            }
            ElasticsearchSession session = (ElasticsearchSession)o;
            return Objects.equals(this.getId(), session.getId());
        }

        public int hashCode() {
            return Objects.hash(this.getId());
        }
    }
}
