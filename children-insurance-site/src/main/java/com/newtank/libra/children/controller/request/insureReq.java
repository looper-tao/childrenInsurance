package com.newtank.libra.children.controller.request;

import com.newtank.libra.children.entity.ChildrenInsuarnce;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by looper on 2017/10/25.
 */
@Getter
@Setter
public class insureReq {
  //起保时间
  @NotNull(message = "起保日期不能为空！")
  private Date startDate;
  //投保人姓名
  @NotNull(message = "投保人姓名不能为空！")
  private String applicantName;
  //投保人证件类型
  private ChildrenInsuarnce.CardType applicantCardType;
  //投保人证件号
  @NotNull(message = "投保人证件号不能为空！")
  private String applicantCardCode;
  //手机号
  @NotNull(message = "手机号不能为空！")
  private String mobile;
  //邮箱
  @NotNull(message = "邮箱不能为空！")
  private String email;
  //被保人姓名
  @NotNull(message = "被保人姓名不能为空！")
  private String insurantName;
  //被保人证件类型
  private ChildrenInsuarnce.CardType insurantCardType;
  //被保人证件号
  @NotNull(message = "被保人证件号不能为空！")
  private String insurantCardCode;
  //与投保人关系
  private ChildrenInsuarnce.Relation relation;

}
