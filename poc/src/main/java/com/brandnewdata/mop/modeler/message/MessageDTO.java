package com.brandnewdata.mop.modeler.message;

import lombok.Data;

/**
 * The type Message dto.
 *
 * @author caiwillie
 */
@Data
public class MessageDTO {

    /**
     * 类型
     *
     * 新增用户: create_user
     */
    private String type;

    /**
     * 绑定关系
     */
    private String correlationKey;

    /**
     * 消息内容
     */
    private String content;

}
