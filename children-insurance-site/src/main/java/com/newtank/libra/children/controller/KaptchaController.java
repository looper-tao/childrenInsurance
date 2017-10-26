package com.newtank.libra.children.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Controller
public class KaptchaController {

  private static final Logger LOGGER = LoggerFactory.getLogger(KaptchaController.class);
  private Cache<String, String> kaptchaCache;
  @Value("${sms.kaptcha.expire-period:300}")
  private int kaptchaExpirePeriod;


  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    //验证码值缓存
    kaptchaCache = CacheBuilder.newBuilder()
        .maximumSize(100000L)
        .expireAfterWrite(kaptchaExpirePeriod, TimeUnit.SECONDS)
        .build();
  }

  public boolean validateKaptcha(String key, String uniqueId, String kaptchaValue, HttpServletRequest request) {
    String kaptchaKey = key.toUpperCase() + "_" + uniqueId.toUpperCase();

    String currentValue = kaptchaCache.getIfPresent(kaptchaKey);

    //清除图形验证码
    kaptchaCache.invalidate(kaptchaKey);
    if (currentValue != null && currentValue.equals(kaptchaValue)) {
      return true;
    } else {
      return false;
    }
  }

  @RequestMapping("/{key}/kaptcha")
  public void getKaptcha(@PathVariable String key, String uniqueId, HttpServletResponse response)
      throws Exception {
    String kaptchaKey = key.toUpperCase() + "_" + uniqueId.toUpperCase();

    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    response.setHeader("Pragma", "no-cache");
    response.setContentType("image/jpeg");

    // create the text for the image
    String kaptchaText = getKaptchaProducer().createText();
    kaptchaCache.put(kaptchaKey, kaptchaText);

    BufferedImage bi = getKaptchaProducer().createImage(kaptchaText);
    ServletOutputStream out = response.getOutputStream();
    ImageIO.write(bi, "jpg", out);
    try {
      out.flush();
    } finally {
      out.close();
    }
  }

  private DefaultKaptcha getKaptchaProducer() {
    Properties properties = new Properties();
    properties.setProperty("kaptcha.border", "no");

    DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
    defaultKaptcha.setConfig(new Config(properties));

    return defaultKaptcha;
  }
}