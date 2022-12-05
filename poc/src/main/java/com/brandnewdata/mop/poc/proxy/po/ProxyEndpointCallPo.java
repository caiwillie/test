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
 * @author caiwilie
 * @since 2022-12-05
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

    private Date startTime;

    private Long endpointId;

    private String ipAddress;

    private String macAddress;

    private String userAgent;

    private String httpMethod;

    private String httpStatus;

    private String requestQuery;

    private String requestBody;

    private Integer timeConsuming;

    private String responseBody;

    private String errorMessage;


    public static final String ID = "id";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String START_TIME = "start_time";

    public static final String ENDPOINT_ID = "endpoint_id";

    public static final String IP_ADDRESS = "ip_address";

    public static final String MAC_ADDRESS = "mac_address";

    public static final String USER_AGENT = "user_agent";

    public static final String HTTP_METHOD = "http_method";

    public static final String HTTP_STATUS = "http_status";

    public static final String REQUEST_QUERY = "request_query";

    public static final String REQUEST_BODY = "request_body";

    public static final String TIME_CONSUMING = "time_consuming";

    public static final String RESPONSE_BODY = "response_body";

    public static final String ERROR_MESSAGE = "error_message";

}
