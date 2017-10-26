package com.newtank.libra.children.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by looper on 2017/10/25.
 */
@Getter
@Setter
@Entity
@Table(name = "t_libra_children_insurance")
public class ChildrenInsuarnce {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  //起保时间
  @Column(name = "start_date")
  private String startDate;
  //终保日期
  @Column(name = "end_date")
  private String endDate;
  //投保人姓名
  @Column(name = "applicant_name")
  private String applicantName;
  //投保人证件类型
  @Column(name = "applicant_card_type")
  @Enumerated(EnumType.STRING)
  private CardType applicantCardType;
  //投保人证件号
  @Column(name = "applicant_card_code")
  private String applicantCardCode;
  //投保人生日
  @Column(name = "applicant_birthday")
  private String applicantBirthday;
  //手机号
  @Column(name = "mobile")
  private String mobile;
  //邮箱
  @Column(name = "email")
  private String email;
  //被保人姓名
  @Column(name = "insurant_name")
  private String insurantName;
  //被保人证件类型
  @Column(name = "insurant_card_type")
  @Enumerated(EnumType.STRING)
  private CardType insurantCardType;
  //被保人证件号
  @Column(name = "insurant_card_code")
  private String insurantCardCode;
  //被保人生日
  @Column(name = "insurant_birthday")
  private String insurantBirthday;
  //被保人性别
  @Column(name = "insurant_sex")
  private int insurantSex;
  //与投保人关系
  @Column(name = "relation")
  @Enumerated(EnumType.STRING)
  private Relation relation;
  //创建时间
  @Column(name = "create_date")
  private Date createDate;
  //保险总金额
  @Column(name = "total_price")
  private long totalPrice;
  //支付金额
  @Column(name = "pay_price")
  private long payPrice;
  //基本保额
  @Column(name = "genes")
  private String genes;
  //订单状态
  @Column(name = "order_status")
  private OrderStatus orderStatus;
  //投保单号
  @Column(name = "insure_num")
  private String insureNum;
  //支付链接
  @Column(name = "pay_url")
  private String payUrl;
  //是否可以下载订单
  @Column(name = "is_download")
  private Boolean isDownload;
  //支付时间
  @Column(name = "pay_time")
  private Date payTime;

  //保单状态
  public enum OrderStatus {
    //待支付,已支付,已拒绝,已失效
    WAIT_PAY, PAYED,EXPIRED;
  }

  @AllArgsConstructor
  public enum Relation{
    //父亲,母亲
    FATHER(6,1),MOTHER(7,0);
    @Getter
    private int relation;
    @Getter
    private int sex;
  }

  @AllArgsConstructor
  public enum CardType{
    //身份证,护照,驾照,其它
    IDCARD(1),PASSPORT(2),DRIVER_LICENSE(4),OTHER(99);

    @Getter
    private int type;

  }
  //支付方式
  @AllArgsConstructor
  public enum PayWay {
    ALIPAY(1), UNIONPAY(3), TENPAY(14), WXPAY(21), BYBANK(-11);

    @Getter
    private int way;

  }
}
