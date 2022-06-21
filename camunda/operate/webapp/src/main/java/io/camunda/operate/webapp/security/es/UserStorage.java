package io.camunda.operate.webapp.security.es;

import com.fasterxml.jackson.core.JsonProcessingException;
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
@Profile({"!ldap-auth & !sso-auth & !identity-auth"})
@DependsOn({"schemaStartup"})
public class UserStorage extends AbstractReader {
   private static final Logger logger = LoggerFactory.getLogger(UserStorage.class);
   private static final XContentType XCONTENT_TYPE;
   @Autowired
   private UserIndex userIndex;

   public UserEntity getByUserId(String userId) {
      SearchRequest searchRequest = (new SearchRequest(new String[]{this.userIndex.getAlias()})).source((new SearchSourceBuilder()).query(QueryBuilders.termQuery("userId", userId)));

      try {
         SearchResponse response = this.esClient.search(searchRequest, RequestOptions.DEFAULT);
         if (response.getHits().getTotalHits().value == 1L) {
            return (UserEntity)ElasticsearchUtil.fromSearchHit(response.getHits().getHits()[0].getSourceAsString(), this.objectMapper, UserEntity.class);
         } else if (response.getHits().getTotalHits().value > 1L) {
            throw new NotFoundException(String.format("Could not find unique user with userId '%s'.", userId));
         } else {
            throw new NotFoundException(String.format("Could not find user with userId '%s'.", userId));
         }
      } catch (IOException var5) {
         String message = String.format("Exception occurred, while obtaining the user: %s", var5.getMessage());
         throw new OperateRuntimeException(message, var5);
      }
   }

   public void create(UserEntity user) {
      try {
         IndexRequest request = (new IndexRequest(this.userIndex.getFullQualifiedName())).id(user.getId()).source(this.userEntityToJSONString(user), XCONTENT_TYPE);
         this.esClient.index(request, RequestOptions.DEFAULT);
      } catch (Exception var3) {
         logger.error("Could not create user with userId {}", user.getUserId(), var3);
      }

   }

   protected String userEntityToJSONString(UserEntity aUser) throws JsonProcessingException {
      return this.objectMapper.writeValueAsString(aUser);
   }

   static {
      XCONTENT_TYPE = XContentType.JSON;
   }
}
