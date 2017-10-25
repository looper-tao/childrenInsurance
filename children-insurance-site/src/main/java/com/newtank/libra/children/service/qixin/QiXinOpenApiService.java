package com.newtank.libra.children.service.qixin;

import com.huize.qixin.api.req.BaseReq;
import com.newtank.libra.children.conf.QiXinConfig;
import com.qixin.openapi.client.OpenApiRemoteOperation;
import com.qixin.openapi.client.common.ProxyFactory;
import com.qixin.openapi.conf.Configure;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class QiXinOpenApiService {

  protected OpenApiRemoteOperation operation = ProxyFactory.create(OpenApiRemoteOperation.class);

  @Autowired
  protected QiXinConfig config;

  protected <T extends BaseReq> T initBaseReq(Class<T> baseReqClass) {
    T baseReq;
    try {
      baseReq = baseReqClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      return null;
    }

    baseReq.setTransNo(genTransactionNum());
    baseReq.setPartnerId(config.getPartnerId());

    return baseReq;
  }

  /**
   * 生成交易流水号
   *
   * @return
   */
  protected String genTransactionNum() {
    return "NEWTANK" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + RandomStringUtils.randomAlphanumeric(4);
  }

  @PostConstruct
  public void init() {
    Configure.Channel.partnerId = config.getPartnerId();
    Configure.Channel.channelKey = config.getSignKey();
    Configure.Request.baseUrl = config.getApiUrl();
  }

}
