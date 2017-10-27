package com.newtank.libra.children.controller.response;

import lombok.*;

/**
 * Created by looper on 2017/10/10.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChildrenInsurancePayResp {
  @NonNull
  private String insureNum;
  @NonNull
  private String payUrl;
  private String token;

}
