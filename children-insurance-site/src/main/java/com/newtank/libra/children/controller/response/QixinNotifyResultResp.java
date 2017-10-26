package com.newtank.libra.children.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by looper on 2017/10/16.
 */
@Getter
@Setter
@AllArgsConstructor
public class QixinNotifyResultResp {
  private Boolean state;
  private String failMsg;

  public QixinNotifyResultResp(Boolean state) {
    this.state = state;
  }
}
