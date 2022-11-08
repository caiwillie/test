package com.brandnewdata.mop.poc.scene.request;

import lombok.Data;

import java.util.List;

@Data
public class ExportReq {
    private String version;
    private List<String> processIds;
}
