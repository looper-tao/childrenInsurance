package com.newtank.libra.children.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "huize.qixin")
@Component
@Getter
@Setter
public class QiXinConfig {

  private Integer partnerId;
  private String signKey;
  private String apiUrl;

}
