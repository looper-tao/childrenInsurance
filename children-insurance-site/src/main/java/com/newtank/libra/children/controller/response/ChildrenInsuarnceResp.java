package com.newtank.libra.children.controller.response;

import com.newtank.libra.children.entity.ChildrenInsuarnce;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by looper on 2017/10/27.
 */
@Getter
@Setter
public class ChildrenInsuarnceResp {
  private long id;
  //起保时间
  private String startDate;
  //终保日期
//  private String endDate;
  //投保人姓名
  private String applicantName;
  //投保人证件类型
  private ChildrenInsuarnce.CardType applicantCardType;
  //投保人证件号
  private String applicantCardCode;
  //投保人生日
  private String applicantBirthday;
  //手机号
  private String mobile;
  //邮箱
  private String email;
  //被保人姓名
  private String insurantName;
  //被保人证件类型
  private ChildrenInsuarnce.CardType insurantCardType;
  //被保人证件号
  private String insurantCardCode;
  //被保人生日
  private String insurantBirthday;
  //被保人性别
  private int insurantSex;
  //与投保人关系
  private ChildrenInsuarnce.Relation relation;
  //创建时间
  private Date createDate;
  //保险总金额
  private long totalPrice;
  //支付金额
  private long payPrice;
  //基本保额
  private String genes;
  //订单状态
  private ChildrenInsuarnce.OrderStatus orderStatus;
  //投保单号
  private String insureNum;
  //支付链接
  private String payUrl;
  //是否可以下载订单
  private Boolean isDownload;
  //支付时间
  private Date payTime;

  public ChildrenInsuarnceResp (ChildrenInsuarnce childrenInsuarnce){
    this.id = childrenInsuarnce.getId();
    this.startDate = childrenInsuarnce.getStartDate();
    this.applicantName = childrenInsuarnce.getApplicantName();
    this.applicantCardType = childrenInsuarnce.getApplicantCardType();
    this.applicantCardCode = childrenInsuarnce.getApplicantCardCode();
    this.mobile = childrenInsuarnce.getMobile();
    this.email = childrenInsuarnce.getEmail();
    this.insurantName = childrenInsuarnce.getInsurantName();
    this.insurantCardType = childrenInsuarnce.getInsurantCardType();
    this.insurantCardCode = childrenInsuarnce.getInsurantCardCode();
    this.relation = childrenInsuarnce.getRelation();
    this.createDate = childrenInsuarnce.getCreateDate();
    this.totalPrice = childrenInsuarnce.getTotalPrice();
    this.genes = childrenInsuarnce.getGenes();
    this.orderStatus = childrenInsuarnce.getOrderStatus();
    this.insureNum = childrenInsuarnce.getInsureNum();
    this.payUrl = childrenInsuarnce.getPayUrl();
    this.isDownload = childrenInsuarnce.getIsDownload();
  }
}
