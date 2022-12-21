package com.brandnewdata.mop.poc.process.service;

import com.brandnewdata.mop.poc.constant.ProcessConst;
import com.brandnewdata.mop.poc.process.dto.BpmnXmlDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProcessDeployService2Test {

    @Autowired
    private IProcessDeployService2 processDeployService;

    @Test
    void test() {
        BpmnXmlDto bpmnXmlDto = new BpmnXmlDto();
        bpmnXmlDto.setProcessId("Process_ae47c455-52d3-4316-a8bf-fcd9033973b2");
        bpmnXmlDto.setProcessName("测试流程120901");
        bpmnXmlDto.setProcessXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n" +
                "  <bpmn2:process id=\"Process_ae47c455-52d3-4316-a8bf-fcd9033973b2\" name=\"测试流程120901\" isExecutable=\"false\">\n" +
                "    <bpmn2:startEvent id=\"StartEvent_1\" name=\"开始事件\">\n" +
                "      <bpmn2:outgoing>Flow_06f5xr1</bpmn2:outgoing>\n" +
                "    </bpmn2:startEvent>\n" +
                "    <bpmn2:endEvent id=\"Event_1xfu1r6\" name=\"结束事件\">\n" +
                "      <bpmn2:incoming>Flow_06f5xr1</bpmn2:incoming>\n" +
                "    </bpmn2:endEvent>\n" +
                "    <bpmn2:sequenceFlow id=\"Flow_06f5xr1\" sourceRef=\"StartEvent_1\" targetRef=\"Event_1xfu1r6\" />\n" +
                "  </bpmn2:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_ae47c455-52d3-4316-a8bf-fcd9033973b2\">\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_06f5xr1_di\" bpmnElement=\"Flow_06f5xr1\">\n" +
                "        <di:waypoint x=\"530\" y=\"104\" />\n" +
                "        <di:waypoint x=\"530\" y=\"156\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n" +
                "        <dc:Bounds x=\"398\" y=\"56\" width=\"263\" height=\"48\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1xfu1r6_di\" bpmnElement=\"Event_1xfu1r6\">\n" +
                "        <dc:Bounds x=\"398\" y=\"156\" width=\"263\" height=\"48\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn2:definitions>\n");
        processDeployService.snapshotDeploy2(bpmnXmlDto, 1L, ProcessConst.PROCESS_BIZ_TYPE__SCENE);
        return;
    }

}