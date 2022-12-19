package com.brandnewdata.mop.poc.lock;

import cn.hutool.core.util.HashUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.Test;

public class HashTest {

    @Test
    void test1() {
        int a = HashUtil.fnvHash("a");
        String a1 = DigestUtil.md5Hex("a");
        return;
    }
}
