package com.newtank.libra.children.controller.request;

import com.newtank.libra.children.entity.ChildrenInsuarnce;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by looper on 2017/10/25.
 */
@Getter
@Setter
public class InsureReq {
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
  //投保人生日
  @NotNull(message = "投保人生日不能为空！")
  private String applicantBirthday;
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
  //被保人生日
  @NotNull(message = "被保人生日不能为空！")
  private String insurantBirthday;
  //被保人性别
  @NotNull(message = "被保人性别不能为空")
  private int insurantSex;
  //与投保人关系
  private ChildrenInsuarnce.Relation relation;
  //投保金额
  private long totalPrice;
  //基本保额
  private String genes;
  @NotNull(message = "验证码不能为空！")
  private String vcode;


  public ChildrenInsuarnce buildChildrenInsuarnce(){
    ChildrenInsuarnce childrenInsuarnce = new ChildrenInsuarnce();
    //投保开始日期
    childrenInsuarnce.setStartDate(getStartDateFormat());
    //投保结束日期
    childrenInsuarnce.setEndDate(getEndDateFormat());
    childrenInsuarnce.setApplicantName(this.applicantName);
    childrenInsuarnce.setApplicantCardType(this.applicantCardType);
    childrenInsuarnce.setApplicantCardCode(this.applicantCardCode);
    childrenInsuarnce.setApplicantBirthday(this.applicantBirthday);
    childrenInsuarnce.setMobile(this.mobile);
    childrenInsuarnce.setEmail(this.email);
    childrenInsuarnce.setInsurantName(this.insurantName);
    childrenInsuarnce.setInsurantCardType(this.insurantCardType);
    childrenInsuarnce.setInsurantCardCode(this.insurantCardCode);
    childrenInsuarnce.setInsurantBirthday(this.insurantBirthday);
    childrenInsuarnce.setInsurantSex(this.insurantSex);
    childrenInsuarnce.setRelation(this.relation);
    childrenInsuarnce.setTotalPrice(this.totalPrice);
    childrenInsuarnce.setGenes(this.genes+"万元");

    return childrenInsuarnce;
  }

  /**
   * 获取终保日期
   *
   * @return
   */
  private String getEndDateFormat() {
    return new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addYears(this.startDate, 1));
  }

  /**
   * 获取起保日期
   *
   * @return
   */
  private String getStartDateFormat() {
    return new SimpleDateFormat("yyyy-MM-dd").format(this.startDate);
  }

}
