package com.brandnewdata.mop.api.scene;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListReq {
    private List<Long> idList;
}
