package com.brandnewdata.mop.poc.util;

import cn.hutool.core.bean.BeanUtil;
import com.brandnewdata.mop.poc.scene.po.ScenePo;
import com.brandnewdata.mop.poc.scene.po.ScenePoExt;

public abstract class AbstractPoExt<T> {

    public static ScenePoExt wrapper(ScenePo po) {
        ScenePoExt ret = new ScenePoExt();
        BeanUtil.copyProperties(po, ret);
        return ret;
    }

}
