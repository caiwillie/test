package com.brandnewdata.mop.poc.process.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.brandnewdata.mop.poc.error.ErrorMessage;
import com.brandnewdata.mop.poc.process.parser.dto.Action;

import java.util.regex.Pattern;

public class ProcessUtil {

    private static final Pattern PROCESS_PATERN = Pattern.compile("[a-zA-Z0-9\\-_]+");

    public static String convertProcessId(String id) {
        Assert.notNull(id, "流程ID不能为空");
        // . 替换为 _，: 替换为 __
        id = StrUtil.replace(id, ".", "_");
        id = StrUtil.replace(id, ":", "__");
        checkProcessId(id);
        return id;
    }

    public static void checkProcessId(String id) {
        Assert.notNull(id, "流程ID不能为空");
        Assert.isTrue(ReUtil.isMatch(PROCESS_PATERN, id), "流程ID只能包含大小写英文、数字、-、_");
    }

    /**
     * 根据触发器/操作的类型判断 触发器
     * @param type
     * @return
     */
    public static Action parseAction(String type) {
        Assert.notNull(type, "type不能为空");
        String[] arr = type.split(":");
        Assert.isTrue(arr.length == 3, ErrorMessage.CHECK_ERROR("触发器或者操作类型错误", type));
        String groupId = arr[0];
        Assert.notEmpty(groupId, ErrorMessage.NOT_NULL("开发者"));
        String version = arr[2];
        Assert.notEmpty(version, ErrorMessage.NOT_NULL("连接器版本"));

        // 解析 连接器id.操作或触发器id
        arr = arr[1].split("\\.");
        Assert.isTrue(arr.length == 2, ErrorMessage.CHECK_ERROR("触发器或者连接器类型错误", type));
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

    public static String generateProcessId() {
        String uuid = IdUtil.simpleUUID();
        return StrUtil.format("Process_{}", uuid);
    }

}
