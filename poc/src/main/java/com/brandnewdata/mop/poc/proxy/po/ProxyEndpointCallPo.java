package com.brandnewdata.mop.poc.proxy.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2022-12-02
 */
@Getter
@Setter
@TableName("mop_proxy_endpoint_call")
public class ProxyEndpointCallPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Long endpointId;

    private String ip;

    private String mac;

    private String userAgent;

    private String httpMethod;

    private String httpStatus;

    private String httpQuery;

    private String httpBody;

    private Integer timeConsuming;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String ENDPOINT_ID = "endpoint_id";

    public static final String IP = "ip";

    public static final String MAC = "mac";

    public static final String USER_AGENT = "user_agent";

    public static final String HTTP_METHOD = "http_method";

    public static final String HTTP_STATUS = "http_status";

    public static final String HTTP_QUERY = "http_query";

    public static final String HTTP_BODY = "http_body";

    public static final String TIME_CONSUMING = "time_consuming";

}
