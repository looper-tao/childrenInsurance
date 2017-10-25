package com.newtank.libra.children.service.sms;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.newtank.libra.children.exception.OperationFailedException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by looper on 2017/9/27.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@Service
public class SmsSendService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmsSendService.class);

  private static String GLOBAL_COUNT_PREFIX = "SMS_GLOBAL_COUNT_";

  private static String VCODE_PREFIX = "SMS_VCODE_";
  private static String VCODE_INTERVAL_PREFIX = "SMS_VCODE_INTERVAL_";
  private static String VCODE_COUNT_PREFIX = "SMS_VCODE_COUNT_";
  private static String VCODE_VERIFY_PREFIX = "SMS_VCODE_VERIFY_";

  @Value("#{'${sms.global.white-list:0}'.split(',')}")
  private List<String> globalWhiteList;


  private Cache<String, Integer> globalCountCache;

  @Value("${sms.global.send-max:-1}")
  private int globalSendMax;
  @Value("${sms.global.count-period:86400}")
  private int globalCountPeriod;

  private Cache<String, Integer> vcodeCountCache;

  @Value("${sms.vcode.send-max:10}")
  private int vcodeSendMax;
  @Value("${sms.vcode.count-period:86400}")
  private int vcodeCountPeriod;

  private Cache<String, Date> vcodeIntervalCache;
  @Value("${sms.vcode.send-interval:60}")
  private int vcodeSendInterval;

  private Cache<String, String> vcodeCache;
  @Value("${sms.vcode.expire-period:300}")
  private int vcodeExpirePeriod;

  private Cache<String, Integer> vcodeVerifyCache;

  @Value("${sms.vcode.verify-max:10}")
  private int vcodeVerifyMax;
  @Value("${sms.vcode.verify-period:60}")
  private int vcodeVerifyPeriod;


  @Autowired
  private SMSServiceFactory smsServiceFactory;


  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    //全局发送限制缓存
    globalCountCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(globalCountPeriod, TimeUnit.SECONDS)
        .build();

    //验证码发送限制缓存
    vcodeCountCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(vcodeCountPeriod, TimeUnit.SECONDS)
        .build();
    //验证码发送频次缓存
    vcodeIntervalCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(vcodeSendInterval, TimeUnit.SECONDS)
        .build();
    //验证码值缓存
    vcodeCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(vcodeExpirePeriod, TimeUnit.SECONDS)
        .build();

    //验证码验证频次缓存
    vcodeVerifyCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(vcodeVerifyPeriod, TimeUnit.SECONDS)
        .build();
  }


  /**
   * 全局短信发送检测
   */
  private void globalSendCheck(String mobile) throws OperationFailedException {
    if (globalWhiteList == null || (globalWhiteList.size() == 1 && globalWhiteList.get(0).equals("-1")) || globalWhiteList.contains(mobile)) {
      return;
    }
    if (globalSendMax > -1) {
      // globalSendMax设为0 表示全局禁止发送
      Integer globalCount = globalCountCache.getIfPresent(GLOBAL_COUNT_PREFIX + mobile);
      if (globalCount != null && globalCount > globalSendMax) {
        throw new OperationFailedException("短信发送超出限制");
      }
    }
  }

  /**
   * 发送短信
   */
  @Async
  public void send(String mobilePhone, String content) throws OperationFailedException {
    globalSendCheck(mobilePhone);
    LOGGER.info("尝试向" + mobilePhone + "发送'" + content + "'");

    SMSService messageSendService = smsServiceFactory.getMessageSendService();
    if (messageSendService != null) {
      smsServiceFactory.messageSendRouter(messageSendService.send(mobilePhone, content),
          messageSendService.getSmsProvider().getProviderCode());
    } else {
      LOGGER.error("无法获取短信服务，请检查服务配置！");
    }
  }

  /**
   * 验证码发送检测
   */
  private void vcodeSendCheck(String mobile) throws OperationFailedException {
    if (globalWhiteList == null || (globalWhiteList.size() == 1 && globalWhiteList.get(0).equals("-1")) || globalWhiteList.contains(mobile)) {
      return;
    }
    if (vcodeSendMax > -1) {
      // vcodeSendMax设为0 表示全局禁止发送
      Integer vcodeCount = vcodeCountCache.getIfPresent(VCODE_COUNT_PREFIX + mobile);
      if (vcodeCount != null && vcodeCount > vcodeSendMax) {
        throw new OperationFailedException("验证码发送超出限制");
      }
    }
    if (vcodeIntervalCache.getIfPresent(VCODE_INTERVAL_PREFIX + mobile) != null) {
      throw new OperationFailedException("验证码发送过于频繁,请稍后再试");
    }
  }

  /**
   * 发送验证码
   */
  public void vcodeSend(String mobilePhone, String signName) throws OperationFailedException {
    vcodeSendCheck(mobilePhone);
    String vcode = vcodeCache.getIfPresent(VCODE_PREFIX + mobilePhone);
    if (StringUtils.isBlank(vcode)) {
      vcode = RandomStringUtils.randomNumeric(6);
      vcodeCache.put(VCODE_PREFIX + mobilePhone, vcode);
    }
    String content = String.format("【%s】您的短信验证码是：%s, %d 分钟内有效。如非本人操作，请忽略本信息。",
        signName,
        vcode,
        (vcodeExpirePeriod / 60));

    send(mobilePhone, content);

    vcodeIntervalCache.put(VCODE_INTERVAL_PREFIX + mobilePhone, new Date());
    Integer vcodeCount = vcodeCountCache.getIfPresent(VCODE_COUNT_PREFIX + mobilePhone);
    if (vcodeCount == null) {
      vcodeCountCache.put(VCODE_COUNT_PREFIX + mobilePhone, 1);
    } else {
      vcodeCountCache.put(VCODE_COUNT_PREFIX + mobilePhone, ++vcodeCount);
    }
  }


  /**
   * 验证频率检测，默认一分钟内只能验证10次
   */
  private void vcodeVerifyCheck(String mobilePhone) throws OperationFailedException {
    //验证次数判断,对于暴力验证予以拒绝
    Integer verifyCount = vcodeVerifyCache.getIfPresent(VCODE_VERIFY_PREFIX + mobilePhone);
    if (verifyCount != null && verifyCount > vcodeVerifyMax) {
      throw new OperationFailedException("验证码验证过于频繁,请稍后再试");
    }
  }


  /**
   * 验证验证码是否正确
   */
  public void verify(String mobilePhone, String captcha) throws OperationFailedException {
    vcodeVerifyCheck(mobilePhone);

    String cachedValue = vcodeCache.getIfPresent(VCODE_PREFIX + mobilePhone);
    if (cachedValue == null) {
      throw new OperationFailedException("验证码已过期");
    }
    if (!cachedValue.equals(captcha)) {
      throw new OperationFailedException("验证码错误");
    }

    Integer verifyCount = vcodeVerifyCache.getIfPresent(VCODE_VERIFY_PREFIX + mobilePhone);
    if (verifyCount == null) {
      vcodeVerifyCache.put(VCODE_VERIFY_PREFIX + mobilePhone, 1);
    } else {
      vcodeVerifyCache.put(VCODE_VERIFY_PREFIX + mobilePhone, ++verifyCount);
    }
  }


  /**
   * 验证验证码是否正确，只能验证一次
   */
  public void delete(String mobilePhone) throws OperationFailedException {
    vcodeCache.invalidate(VCODE_PREFIX + mobilePhone);
  }
}
