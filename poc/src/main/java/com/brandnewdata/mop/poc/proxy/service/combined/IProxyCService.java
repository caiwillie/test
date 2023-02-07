package com.brandnewdata.mop.poc.proxy.service.combined;

public interface IProxyCService {

    void deleteById(Long id);

    String inspect(Long proxyId, String format);

    void importProxy(String content, String format);
}
