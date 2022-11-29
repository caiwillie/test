package com.brandnewdata.mop.poc.scene.service;

import com.brandnewdata.mop.poc.scene.dto.VersionProcessDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ISceneVersionServiceTest {

    private static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:modeler=\"http://camunda.org/schema/modeler/1.0\" id=\"Definitions_1idaram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"5.4.1\" modeler:executionPlatform=\"Camunda Cloud\" modeler:executionPlatformVersion=\"8.1.0\">\n" +
            "  <bpmn:process id=\"Process_1oazfp2\" isExecutable=\"true\">\n" +
            "    <bpmn:startEvent id=\"StartEvent_1\">\n" +
            "      <bpmn:outgoing>Flow_1xt1qpg</bpmn:outgoing>\n" +
            "    </bpmn:startEvent>\n" +
            "    <bpmn:endEvent id=\"Event_1u2k8jw\">\n" +
            "      <bpmn:incoming>Flow_1xt1qpg</bpmn:incoming>\n" +
            "    </bpmn:endEvent>\n" +
            "    <bpmn:sequenceFlow id=\"Flow_1xt1qpg\" sourceRef=\"StartEvent_1\" targetRef=\"Event_1u2k8jw\" />\n" +
            "  </bpmn:process>\n" +
            "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
            "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1oazfp2\">\n" +
            "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n" +
            "        <dc:Bounds x=\"152\" y=\"82\" width=\"36\" height=\"36\" />\n" +
            "      </bpmndi:BPMNShape>\n" +
            "      <bpmndi:BPMNShape id=\"Event_1u2k8jw_di\" bpmnElement=\"Event_1u2k8jw\">\n" +
            "        <dc:Bounds x=\"242\" y=\"82\" width=\"36\" height=\"36\" />\n" +
            "      </bpmndi:BPMNShape>\n" +
            "      <bpmndi:BPMNEdge id=\"Flow_1xt1qpg_di\" bpmnElement=\"Flow_1xt1qpg\">\n" +
            "        <di:waypoint x=\"188\" y=\"100\" />\n" +
            "        <di:waypoint x=\"242\" y=\"100\" />\n" +
            "      </bpmndi:BPMNEdge>\n" +
            "    </bpmndi:BPMNPlane>\n" +
            "  </bpmndi:BPMNDiagram>\n" +
            "</bpmn:definitions>\n";

    @Autowired
    private ISceneVersionService sceneVersionService;

    @Test
    void saveProcess() {
        VersionProcessDto dto = new VersionProcessDto();
        dto.setId(54L);
        dto.setProcessId("Process_1oazfp2");
        dto.setProcessName("caiwillie测试流程2");
        dto.setVersionId(66L);
        dto.setProcessXml(xml);
        dto.setProcessImg(xml);
        sceneVersionService.saveProcess(dto);
    }
}