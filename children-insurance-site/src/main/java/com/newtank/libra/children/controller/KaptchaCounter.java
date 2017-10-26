package com.newtank.libra.children.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.newtank.libra.children.conf.KaptchaConfig;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class KaptchaCounter {

  private static final String KAPTCHA_COUNT_SUFFIX = "_KAPTCHA_COUNT";

  private Cache<String, Integer> cache;

  private KaptchaConfig config;

  public KaptchaCounter(KaptchaConfig config) {
    this.config = config;
    this.cache = CacheBuilder.newBuilder()
                             .maximumSize(50000L)
                             .expireAfterWrite(config.getCountPeriod(), TimeUnit.SECONDS)
                             .build();
  }

  public void add(String uniqueId) {
    String kaptchaCountKey = config.getKeyCode() + KAPTCHA_COUNT_SUFFIX + "@" + uniqueId;
    Integer count = cache.getIfPresent(kaptchaCountKey);
    if (count == null) {
      cache.put(kaptchaCountKey, 1);
    } else {
      cache.put(kaptchaCountKey, ++count);
    }
  }

  public void reset(String uniqueId) {
    cache.put(config.getKeyCode() + KAPTCHA_COUNT_SUFFIX + "@" + uniqueId, 0);
  }

  public boolean isKaptchaRequired(String uniqueId) {
    String kaptchaCountKey = config.getKeyCode() + KAPTCHA_COUNT_SUFFIX + "@" + uniqueId;
    Integer count = cache.getIfPresent(kaptchaCountKey);
    return count != null && count > config.getRequiredFrom();
  }

}
