package com.brandnewdata.mop.poc.scene.service.combine;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.Map;

public interface IVersionProcessCService {

    VersionProcessDto save(VersionProcessDto dto);

    void debug(VersionProcessDto dto, Map<String, Object> variables);

    void deleteById(Long id);

    void deleteByVersionId(Long versionId);
}
