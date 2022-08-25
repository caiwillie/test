package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.common.dto.Page;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneDTO;
import com.brandnewdata.mop.poc.scene.dto.BusinessSceneProcessDTO;

import java.util.List;

/**
 * @author caiwillie
 */
public interface IBusinessSceneService {

    Page<BusinessSceneDTO> page(int pageNumber, int pageSize, String name);

    BusinessSceneDTO getOne(Long id);

    List<BusinessSceneDTO> listByIds(List<Long> ids);

    BusinessSceneDTO save(BusinessSceneDTO businessSceneDTO);

    BusinessSceneProcessDTO saveProcessDefinition(BusinessSceneProcessDTO businessSceneProcessDTO);

    void deploy(BusinessSceneProcessDTO businessSceneProcessDTO);

}
