/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity
 *  io.camunda.operate.webapp.rest.dto.CreatableFromEntity
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package io.camunda.operate.webapp.rest.dto.dmn;

import io.camunda.operate.entities.dmn.definition.DecisionDefinitionEntity;
import io.camunda.operate.webapp.rest.dto.CreatableFromEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Decision object")
public class DecisionDto
implements CreatableFromEntity<DecisionDto, DecisionDefinitionEntity> {
    @ApiModelProperty(value="Unique id of the decision, must be used when filtering instances by decision ids.")
    private String id;
    private String name;
    private int version;
    private String decisionId;

    public String getId() {
        return this.id;
    }

    public DecisionDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public DecisionDto setName(String name) {
        this.name = name;
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public DecisionDto setVersion(int version) {
        this.version = version;
        return this;
    }

    public String getDecisionId() {
        return this.decisionId;
    }

    public DecisionDto setDecisionId(String decisionId) {
        this.decisionId = decisionId;
        return this;
    }

    public DecisionDto fillFrom(DecisionDefinitionEntity decisionEntity) {
        return this.setId(decisionEntity.getId()).setDecisionId(decisionEntity.getDecisionId()).setName(decisionEntity.getName()).setVersion(decisionEntity.getVersion());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DecisionDto that = (DecisionDto)o;
        if (this.version != that.version) {
            return false;
        }
        if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
            return false;
        }
        if (!(this.name != null ? !this.name.equals(that.name) : that.name != null)) return this.decisionId != null ? this.decisionId.equals(that.decisionId) : that.decisionId == null;
        return false;
    }

    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + this.version;
        result = 31 * result + (this.decisionId != null ? this.decisionId.hashCode() : 0);
        return result;
    }
}
