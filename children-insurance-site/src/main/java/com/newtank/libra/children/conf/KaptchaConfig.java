package com.newtank.libra.children.conf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KaptchaConfig {
  private String keyCode;
  private Integer requiredFrom;
  private Integer countPeriod;
}