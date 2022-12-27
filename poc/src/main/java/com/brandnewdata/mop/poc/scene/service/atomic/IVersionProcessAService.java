package com.brandnewdata.mop.poc.scene.service.atomic;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface IVersionProcessAService {

    Map<Long, VersionProcessDto> fetchOneById(List<Long> idList);
}
