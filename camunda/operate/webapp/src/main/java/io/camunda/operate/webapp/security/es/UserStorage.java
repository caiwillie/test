/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonProcessingException
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  io.camunda.operate.entities.UserEntity
 *  io.camunda.operate.exceptions.OperateRuntimeException
 *  io.camunda.operate.schema.indices.UserIndex
 *  io.camunda.operate.util.ElasticsearchUtil
 *  io.camunda.operate.webapp.es.reader.AbstractReader
 *  io.camunda.operate.webapp.rest.exception.NotFoundException
 *  org.elasticsearch.action.index.IndexRequest
 *  org.elasticsearch.action.search.SearchRequest
 *  org.elasticsearch.action.search.SearchResponse
 *  org.elasticsearch.client.RequestOptions
 *  org.elasticsearch.index.query.QueryBuilder
 *  org.elasticsearch.index.query.QueryBuilders
 *  org.elasticsearch.search.builder.SearchSourceBuilder
 *  org.elasticsearch.xcontent.XContentType
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.DependsOn
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Component
 */
package io.camunda.operate.webapp.security.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.entities.UserEntity;
import io.camunda.operate.exceptions.OperateRuntimeException;
import io.camunda.operate.schema.indices.UserIndex;
import io.camunda.operate.util.ElasticsearchUtil;
import io.camunda.operate.webapp.es.reader.AbstractReader;
import io.camunda.operate.webapp.rest.exception.NotFoundException;
import java.io.IOException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value={"!ldap-auth & !sso-auth & !identity-auth"})
@DependsOn(value={"schemaStartup"})
public class UserStorage
extends AbstractReader {
    private static final Logger logger = LoggerFactory.getLogger(UserStorage.class);
    private static final XContentType XCONTENT_TYPE = XContentType.JSON;
    @Autowired
    private UserIndex userIndex;

    public UserEntity getByUserId(String userId) {
        SearchRequest searchRequest = new SearchRequest(new String[]{this.userIndex.getAlias()}).source(new SearchSourceBuilder().query((QueryBuilder)QueryBuilders.termQuery((String)"userId", (String)userId)));
        try {
            SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value == 1L) {
                return (UserEntity)ElasticsearchUtil.fromSearchHit((String)response.getHits().getHits()[0].getSourceAsString(), (ObjectMapper)this.objectMapper, UserEntity.class);
            }
            if (response.getHits().getTotalHits().value <= 1L) throw new NotFoundException(String.format("Could not find user with userId '%s'.", userId));
            throw new NotFoundException(String.format("Could not find unique user with userId '%s'.", userId));
        }
        catch (IOException e) {
            String message = String.format("Exception occurred, while obtaining the user: %s", e.getMessage());
            throw new OperateRuntimeException(message, (Throwable)e);
        }
    }

    public void create(UserEntity user) {
        try {
            IndexRequest request = new IndexRequest(this.userIndex.getFullQualifiedName()).id(user.getId()).source(this.userEntityToJSONString(user), XCONTENT_TYPE);
            this.esClient.index(request, RequestOptions.DEFAULT);
        }
        catch (Exception t) {
            logger.error("Could not create user with userId {}", (Object)user.getUserId(), (Object)t);
        }
    }

    protected String userEntityToJSONString(UserEntity aUser) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString((Object)aUser);
    }
}
