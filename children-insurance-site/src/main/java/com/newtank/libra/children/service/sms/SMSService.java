package com.newtank.libra.children.service.sms;


import com.newtank.libra.children.entity.SMSProvider;

/**
 * Created by looper on 2017/9/27.
 */
public interface SMSService {

  String CHARSET = "utf-8";

  /**
   * 发送短信
   *
   * @param mobilePhone 要发送的手机号
   * @param content     要发送的短信内容
   * @return 是否发送成功
   */
  boolean send(String mobilePhone, String content);

  /**
   * 获取短信服务提供商配置
   *
   * @return
   */
  SMSProvider getSmsProvider();

  /**
   * 设置短息服务供应商配置
   *
   * @param smsProvider
   */
  void setSmsProvider(SMSProvider smsProvider);

}

