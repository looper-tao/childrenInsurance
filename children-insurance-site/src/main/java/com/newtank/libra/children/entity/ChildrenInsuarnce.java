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
@AllArgsConstructor
@Entity
@Table(name = "t_libra_children_insurance")
public class ChildrenInsuarnce {
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
  //与投保人关系
  @Column(name = "relation")
  @Enumerated(EnumType.STRING)
  private Relation relation;
  //创建时间
  @Column(name = "create_date")
  private Date createDate;

  @AllArgsConstructor
  public enum Relation{
    //父亲,母亲
    FATHER(6),MOTHER(7);
    @Getter
    private int relation;
  }

  @AllArgsConstructor
  public enum CardType{
    //身份证,护照,驾照,其它
    IDCARD(1),PASSPORT(2),DRIVER_LICENSE(4),OTHER(99);

    @Getter
    private int type;

  }
}
