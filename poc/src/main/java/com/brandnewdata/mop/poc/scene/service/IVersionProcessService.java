package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;

import java.util.List;
import java.util.Map;

public interface IVersionProcessService {

    Map<Long, List<VersionProcessDto>> fetchListByVersionId(List<Long> versionIdList, boolean simple);



    Map<String, VersionProcessDto> fetchOneByProcessId(List<String> processIdList);

    Map<Long, Integer> fetchCountByVersionId(List<Long> versionIdList);

    Map<Long, VersionProcessDto> fetchLatestOneByVersionId(List<Long> versionIdList);

    void deleteById(List<Long> idList);
    

}
