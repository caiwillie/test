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
        ListViewProcessInstanceDTO dto = new ListViewProcessInstanceDTO();
        if (entity == null) {
            return null;
        }
        dto.setId(entity.getId());
        dto.setStartDate(Optional.ofNullable(entity.getStartDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        dto.setEndDate(Optional.ofNullable(entity.getEndDate()).map(OffsetDateTime::toLocalDateTime).orElse(null));
        if (entity.getState() == ProcessInstanceState.ACTIVE && entity.isIncident()) {
            dto.setState(ProcessInstanceStateDto.INCIDENT);
        } else {
            dto.setState(ProcessInstanceStateDto.getState(entity.getState()));
        }
        dto.setProcessId(Optional.ofNullable(entity.getProcessDefinitionKey()).map(String::valueOf).orElse(null));
        dto.setBpmnProcessId(entity.getBpmnProcessId());
        dto.setProcessName(entity.getProcessName());
        dto.setProcessVersion(entity.getProcessVersion());

        // dto.setOperations(DtoCreator.create(operations, OperationDto.class));

        dto.setParentInstanceId(Optional.ofNullable(entity.getParentProcessInstanceKey()).map(String::valueOf).orElse(null));
        if (entity.getSortValues() != null) {
            dto.setSortValues(Arrays.stream(entity.getSortValues()).map(String::valueOf).toArray(String[]::new));
        }
        if (entity.getTreePath() != null) {
            String rootInstanceId = new TreePathUtil(entity.getTreePath()).extractRootInstanceId();
            // 如果 rootInstanceId 和 id 不相等，就设置
            if (!entity.getId().equals(rootInstanceId)) {
                dto.setRootInstanceId(rootInstanceId);
            }
        }

        // operation 和 callHierarchy 还没弄好

        return dto;
    }

}
