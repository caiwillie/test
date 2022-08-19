package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.FlowNodeInstanceEntity;
import com.brandnewdata.mop.poc.operate.entity.FlowNodeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class FlowNodeInstanceListDto extends OperateZeebeDto implements FromEntity<FlowNodeInstanceListDto, FlowNodeInstanceEntity> {

    private String flowNodeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private FlowNodeStateDto state;
    private FlowNodeType type;
    private Long incidentKey;
    private Long processInstanceKey;
    private String treePath;
    private int level;
    private Long position;
    private boolean incident;

    private List<FlowNodeInstanceListDto> children;

    @Override
    public FlowNodeInstanceListDto fromEntity(FlowNodeInstanceEntity entity) {
        this.setId(entity.getId());
        this.setFlowNodeId(entity.getFlowNodeId());
        this.setStartDate(Optional.ofNullable(entity.getStartDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        this.setEndDate(Optional.ofNullable(entity.getEndDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        this.setState(FlowNodeStateDto.getState(entity.getState()));
        this.setType(entity.getType());
        this.setIncidentKey(entity.getIncidentKey());
        this.setProcessInstanceKey(entity.getProcessInstanceKey());
        this.setTreePath(entity.getTreePath());
        this.setLevel(entity.getLevel());
        this.setPosition(entity.getPosition());
        this.setIncident(entity.isIncident());
        return this;
    }


}
