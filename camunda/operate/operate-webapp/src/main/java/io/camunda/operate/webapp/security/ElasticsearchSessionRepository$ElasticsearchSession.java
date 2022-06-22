/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.session.ExpiringSession
 *  org.springframework.session.MapSession
 */
package io.camunda.operate.webapp.security;

import java.util.Objects;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;

/*
static class ElasticsearchSession implements ExpiringSession {
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
*/
