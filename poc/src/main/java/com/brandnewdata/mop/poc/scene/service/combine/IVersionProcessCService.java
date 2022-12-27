package com.brandnewdata.mop.poc.scene.service.combine;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

public interface IVersionProcessCService {

    VersionProcessDto save(VersionProcessDto dto);

    void deleteProcess(VersionProcessDto dto);

}
