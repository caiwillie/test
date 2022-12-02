package com.brandnewdata.mop.poc.proxy.po;

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
 * @since 2022-10-11
 */
@Getter
@Setter
@TableName("mop_reverse_proxy_endpoint")
public class ProxyEndpointPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Long proxyId;

    private String location;

    private String description;

    private Integer backendType;

    private String backendConfig;

    private String tag;

    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String PROXY_ID = "proxy_id";

    public static final String LOCATION = "location";

    public static final String DESCRIPTION = "description";

    public static final String BACKEND_TYPE = "backend_type";

    public static final String BACKEND_CONFIG = "backend_config";

    public static final String TAG = "tag";

}
