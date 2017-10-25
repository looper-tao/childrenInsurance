package com.newtank.libra.children.service.sms;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.newtank.libra.children.entity.SMSProvider;
import com.newtank.libra.children.repository.SMSProviderRepository;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by looper on 2017/9/27.
 *
 * @author looper
 * @author trueorfalse.yuan
 */
@SuppressWarnings("WeakerAccess")
@Aspect
@Component
public class SMSServiceFactory {

  private static final String CLASS_NAME_SUFFIX = "Service";

  private static final int MESSAGE_PROVIDER_FAIL_MAX = 10;
  private static final String MESSAGE_PROVIDER_SUFFIX = "_FAIL_COUNT";

  private static Logger LOGGER = LoggerFactory.getLogger(SMSServiceFactory.class);
  private static Cache<String, Integer> cache = CacheBuilder.newBuilder()
      .maximumSize(50000L)
      .expireAfterWrite(10L, TimeUnit.MINUTES)
      .build();
  @Autowired
  private SMSProviderRepository smsProviderRepository;

  @Getter
  @Setter
  private List<SMSProvider> smsProviderList = new ArrayList<>();

  private Map<String, SMSService> smsServiceMap = new LinkedHashMap<>();


  private static void put(String key, Integer value) {
    cache.put(key, value);
  }

  private static Integer get(String key) {
    return cache.getIfPresent(key);
  }

  private static Boolean isAvailable(String providerCode) {
    Integer failCount = get(providerCode + MESSAGE_PROVIDER_SUFFIX);
    return failCount == null || failCount < MESSAGE_PROVIDER_FAIL_MAX;

  }

  @PostConstruct
  private void init() {
    smsProviderList = smsProviderRepository.findByActiveTrueOrderByRankDesc();
    if (smsProviderList == null) {
      throw new IllegalStateException("Please initialize the sms provider config!");
    }
    smsServiceMap = new LinkedHashMap<>();
    for (SMSProvider smsProvider : smsProviderList) {
      try {
        String className = smsProvider.getProviderCode() + CLASS_NAME_SUFFIX;
        String packageName = getClass().getPackage().getName();
        SMSService messageSendService = (SMSService) Class.forName(packageName + "." + className).newInstance();
        if (messageSendService != null) {
          messageSendService.setSmsProvider(smsProvider);
        }
        smsServiceMap.put(smsProvider.getProviderCode(), messageSendService);
      } catch (Exception e) {
        LOGGER.error("Failed to create SMSService instance(provider:" + smsProvider.getProviderCode() + ") - " + e.getMessage());
      }
    }

    if (smsServiceMap.isEmpty()) {
      throw new IllegalStateException("Please check the initialization of the SMSServiceFactory!");
    }
  }

  /**
   * 更新短信供应商列表
   */
  @AfterReturning("@annotation(com.newtank.libra.children.service.sms.SMSServiceFactory.ShortMessageChannelChangeNotice)")
  public void reloadSmsProviderList() {
    init();
  }

  /**
   * 获取消息发送服务实例
   *
   * @return 消息发送服务对象
   */
  public SMSService getMessageSendService() {
    SMSService smsService = null;
    for (SMSProvider smsProvider : smsProviderList) {
      if (smsServiceMap.containsKey(smsProvider.getProviderCode()) && isAvailable(smsProvider.getProviderCode())) {
        smsService = smsServiceMap.get(smsProvider.getProviderCode());
        break;
      }
    }
    if (smsService == null) {
      for (SMSProvider smsProvider : smsProviderList) {
        if (smsServiceMap.containsKey(smsProvider.getProviderCode())) {
          smsService = smsServiceMap.get(smsProvider.getProviderCode());
          break;
        }
      }
    }

    if (smsService == null) {
      throw new IllegalStateException("No available SMSService!");
    }

    return smsService;
  }

  /**
   * 获取消息发送服务实例
   *
   * @return 消息发送服务对象
   */
  public SMSService getMessageSendService(String providerCode) {
    SMSService smsService = smsServiceMap.get(providerCode);
    if (smsService == null) {
      throw new IllegalStateException("The SMSService is unavailable - ProviderCode: " + providerCode);
    }
    return smsService;
  }

  /**
   * 如果某个短信渠道10分钟内发送失败10次，则路由到优先级较低的短信渠道
   *
   * @param sendSuccess  发送是否成功
   * @param providerCode 短信服务提供商编码
   */
  public void messageSendRouter(boolean sendSuccess, String providerCode) {
    if (!sendSuccess) {
      String failCountKey = providerCode + MESSAGE_PROVIDER_SUFFIX;
      Integer failCount = get(failCountKey);
      if (failCount == null) {
        put(failCountKey, 1);
      } else {
        put(failCountKey, ++failCount);
      }
    }
  }


  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ShortMessageChannelChangeNotice {
  }

}
