package com.brandnewdata.mop.poc.proxy.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

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
public class ReverseProxyEndpointEntity implements Serializable {

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
