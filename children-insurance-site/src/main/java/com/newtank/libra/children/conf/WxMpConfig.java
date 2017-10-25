package com.newtank.libra.children.conf;

import lombok.Getter;
import lombok.Setter;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxMpConfig {

  @Autowired
  private Config globalWxMpConfig;

  @Bean
  public WxMpService wxMpService() {
    WxMpService wxMpService = new WxMpServiceImpl();
    WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
    configStorage.setAppId(globalWxMpConfig.getAppId());
    configStorage.setSecret(globalWxMpConfig.getSecret());
    configStorage.setToken(globalWxMpConfig.getToken());
    configStorage.setAesKey(globalWxMpConfig.getAesKey());
    wxMpService.setWxMpConfigStorage(configStorage);
    return wxMpService;
  }

  @Bean(name = "globalWxMpConfig")
  @ConfigurationProperties(prefix = "wx.mp")
  public Config getConfig() {
    return new Config();
  }

  @SuppressWarnings("WeakerAccess")
  @Getter
  @Setter
  public class Config {
    private String appId;
    private String secret;
    private String token;
    private String aesKey;
  }
}
