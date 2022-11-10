package com.brandnewdata.mop.poc.process.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.parser.dto.Action;

public class ProcessUtil {

    public static String convertProcessId(String id) {
        String result = null;
        // . 和 :
        result = StrUtil.replace(id, ".", "_");
        result = StrUtil.replace(id, ":", "__");
        return result;
    }

    /**
     * 根据触发器/操作的类型判断 触发器
     * @param actionFullId
     * @return
     */
    public static Action parseActionInfo(String actionFullId) {
        String[] arr = actionFullId.split(":");
        Assert.isTrue(arr.length == 3, ErrorMessage.CHECK_ERROR("触发器或者操作类型错误", actionFullId));
        String groupId = arr[0];
        Assert.notEmpty(groupId, ErrorMessage.NOT_NULL("开发者"));
        String version = arr[2];
        Assert.notEmpty(version, ErrorMessage.NOT_NULL("连接器版本"));

        // 解析 连接器id.操作或触发器id
        arr = arr[1].split("\\.");
        Assert.isTrue(arr.length == 2, ErrorMessage.CHECK_ERROR("触发器或者连接器类型错误", actionFullId));
        String connectorId = arr[0];
        Assert.notEmpty(connectorId, ErrorMessage.NOT_NULL("连接器 id"));
        String actionId = arr[1];
        Assert.notEmpty(actionId, ErrorMessage.NOT_NULL("触发器 id"));

        Action ret = new Action();
        ret.setConnectorGroup(groupId);
        ret.setConnectorId(connectorId);
        ret.setActionId(actionId);
        ret.setConnectorVersion(version);

        return ret;
    }

}
