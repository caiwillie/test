package com.brandnewdata.mop.poc.proxy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author caiwillie
 * @since 2022-09-26
 */
@Getter
@Setter
@TableName("mop_reverse_proxy")
public class ReverseProxyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String name;

    private Integer protocol;

    private String version;

    private String description;

    private String domain;

    private String tag;

    /**
     * 状态：1 停止，2 运行
     */
    private Integer state;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String NAME = "name";

    public static final String PROTOCOL = "protocol";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String DOMAIN = "domain";

    public static final String TAG = "tag";

    public static final String STATE = "state";

}
