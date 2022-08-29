package com.brandnewdata.mop.poc.operate.dto;

import com.brandnewdata.mop.poc.operate.entity.listview.ProcessInstanceForListViewEntity;
import com.brandnewdata.mop.poc.operate.util.TreePathUtil;
import io.camunda.operate.dto.ProcessInstanceState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class ListViewProcessInstanceDTO extends OperateZeebeDTO {
    private String id;
    private String processId;
    private String processName;
    private Integer processVersion;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ProcessInstanceStateDto state;
    private String bpmnProcessId;
    private boolean hasActiveOperation = false;
    private List<OperationDTO> operations = new ArrayList<>();
    private String parentInstanceId;
    private String rootInstanceId;
    private List<ProcessInstanceReferenceDTO> callHierarchy = new ArrayList<ProcessInstanceReferenceDTO>();
    private String[] sortValues;

    public ListViewProcessInstanceDTO from(ProcessInstanceForListViewEntity entity) {
        if (entity == null) {
            return null;
        }
        this.setId(entity.getId());
        this.setStartDate(Optional.ofNullable(entity.getStartDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        this.setEndDate(Optional.ofNullable(entity.getEndDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        if (entity.getState() == ProcessInstanceState.ACTIVE && entity.isIncident()) {
            this.setState(ProcessInstanceStateDto.INCIDENT);
        } else {
            this.setState(ProcessInstanceStateDto.getState(entity.getState()));
        }
        this.setProcessId(Optional.ofNullable(entity.getProcessDefinitionKey()).map(String::valueOf).orElse(null));
        this.setBpmnProcessId(entity.getBpmnProcessId());
        this.setProcessName(entity.getProcessName());
        this.setProcessVersion(entity.getProcessVersion());

        // dto.setOperations(DtoCreator.create(operations, OperationDto.class));

        this.setParentInstanceId(Optional.ofNullable(entity.getParentProcessInstanceKey()).map(String::valueOf).orElse(null));
        if (entity.getSortValues() != null) {
            this.setSortValues(Arrays.stream(entity.getSortValues()).map(String::valueOf).toArray(String[]::new));
        }
        if (entity.getTreePath() != null) {
            String rootInstanceId = new TreePathUtil(entity.getTreePath()).extractRootInstanceId();
            // 如果 rootInstanceId 和 id 不相等，就设置
            if (!entity.getId().equals(rootInstanceId)) {
                this.setRootInstanceId(rootInstanceId);
            }
        }

        // operation 和 callHierarchy 还没弄好

        return this;
    }

}
