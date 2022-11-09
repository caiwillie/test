package com.brandnewdata.mop.poc.scene.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExportReq {
    private String version;
    private List<String> processIds;
}
