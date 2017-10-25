package com.newtank.libra.children.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by looper on 2017/9/27.
 */
@Getter
@Setter
@Entity
@Table(name = "t_libra_sms_provider")
public class SMSProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String providerCode;

  private String providerName;

  private String sendUrl;

  private String account;

  private String password;

  private boolean active;

  private long rank;

  private Date createAt = new Date();

  private Date updateAt;

  private String receiveUrl;
}
