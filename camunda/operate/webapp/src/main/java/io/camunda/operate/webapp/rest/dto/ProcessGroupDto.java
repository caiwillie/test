/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.camunda.operate.entities.ProcessEntity
 *  io.camunda.operate.webapp.rest.dto.DtoCreator
 *  io.camunda.operate.webapp.rest.dto.ProcessDto
 *  io.camunda.operate.webapp.rest.dto.ProcessGroupDto$ProcessGroupComparator
 *  io.swagger.annotations.ApiModel
 */
package io.camunda.operate.webapp.rest.dto;

import io.camunda.operate.entities.ProcessEntity;
import io.camunda.operate.webapp.rest.dto.DtoCreator;
import io.camunda.operate.webapp.rest.dto.ProcessDto;
import io.camunda.operate.webapp.rest.dto.ProcessGroupDto;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApiModel(value="Process group object", description="Group of processes with the same bpmnProcessId with all versions included")
public class ProcessGroupDto {
    private String bpmnProcessId;
    private String name;
    private List<ProcessDto> processes;

    public String getBpmnProcessId() {
        return this.bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProcessDto> getProcesses() {
        return this.processes;
    }

    public void setProcesses(List<ProcessDto> processes) {
        this.processes = processes;
    }

    public static List<ProcessGroupDto> createFrom(Map<String, List<ProcessEntity>> processesGrouped) {
        ArrayList<ProcessGroupDto> groups = new ArrayList<ProcessGroupDto>();
        processesGrouped.entrySet().stream().forEach(groupEntry -> {
            ProcessGroupDto groupDto = new ProcessGroupDto();
            groupDto.setBpmnProcessId((String)groupEntry.getKey());
            groupDto.setName(((ProcessEntity)((List)groupEntry.getValue()).get(0)).getName());
            groupDto.setProcesses(DtoCreator.create((List)((List)groupEntry.getValue()), ProcessDto.class));
            groups.add(groupDto);
        });
        groups.sort((Comparator<ProcessGroupDto>)new ProcessGroupComparator());
        return groups;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ProcessGroupDto that = (ProcessGroupDto)o;
        return this.bpmnProcessId != null ? this.bpmnProcessId.equals(that.bpmnProcessId) : that.bpmnProcessId == null;
    }

    public int hashCode() {
        return this.bpmnProcessId != null ? this.bpmnProcessId.hashCode() : 0;
    }

    public static class ProcessGroupComparator implements Comparator<ProcessGroupDto> {
        @Override
        public int compare(ProcessGroupDto o1, ProcessGroupDto o2) {
            if (o1.getName() == null && o2.getName() == null) {
                return o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
            }
            if (o1.getName() == null) {
                return 1;
            }
            if (o2.getName() == null) {
                return -1;
            }
            if (o1.getName().equals(o2.getName())) return o1.getBpmnProcessId().compareTo(o2.getBpmnProcessId());
            return o1.getName().compareTo(o2.getName());
        }
    }
}
