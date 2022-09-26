package com.brandnewdata.mop.poc.tag.resp;

import com.brandnewdata.mop.poc.tag.req.TagReq;
import lombok.Data;

@Data
public class TagResp extends TagReq {

    /**
     * id
     */
    private Long id;


    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

}
