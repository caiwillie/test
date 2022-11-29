package com.brandnewdata.mop.poc.scene.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.brandnewdata.mop.poc.scene.dto.SceneVersionDto;
import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SceneVersionServiceTest {

    @Autowired
    private ISceneVersionService sceneVersionService;

    @Test
    void testSaveProcess() {
        String xml = ResourceUtil.readUtf8Str("process/empty_process.xml");
        VersionProcessDto dto = new VersionProcessDto();
        dto.setId(54L);
        dto.setProcessId("Process_1oazfp2");
        dto.setProcessName("caiwillie测试流程2");
        dto.setVersionId(66L);
        dto.setProcessXml(xml);
        dto.setProcessImg(xml);
        sceneVersionService.saveProcess(dto);
    }

    @Test
    void testDebug() {
        SceneVersionDto dto = sceneVersionService.debug(66L, 1L);
        return;
    }
}