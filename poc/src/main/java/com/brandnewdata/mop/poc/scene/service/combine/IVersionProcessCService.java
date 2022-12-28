package com.brandnewdata.mop.poc.scene.service.combine;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;

public interface IVersionProcessCService {

    VersionProcessDto save(VersionProcessDto dto);

    void deleteById(List<Long> idList);
}
