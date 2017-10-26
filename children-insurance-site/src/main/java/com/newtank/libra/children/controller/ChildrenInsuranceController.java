package com.newtank.libra.children.controller;

import com.huize.qixin.api.resp.insure.InsureResp;
import com.newtank.libra.children.conf.KaptchaConfig;
import com.newtank.libra.children.controller.request.InsureReq;
import com.newtank.libra.children.controller.response.ChildrenInsurancePayResp;
import com.newtank.libra.children.controller.response.QixinNotifyResultResp;
import com.newtank.libra.children.entity.ChildrenInsuarnce;
import com.newtank.libra.children.exception.OperationFailedException;
import com.newtank.libra.children.repository.ChildrenInsuranceRepository;
import com.newtank.libra.children.service.ChildrenInsuranceService;
import com.newtank.libra.children.service.sms.SmsSendService;
import com.newtank.libra.children.utils.HttpUtil;
import com.newtank.libra.children.utils.JsonUtil;
import com.qixin.openapi.model.common.CommonResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by looper on 2017/10/25.
 */
@RestController
@RequestMapping("/children/")
public class ChildrenInsuranceController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChildrenInsuranceController.class);

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  private static final String VCODE_KAPTCHA_KEY = "children-vcode";
  private static final int INSURE_HOURS = 23;
  private static final int EXPIRED_TIME = 15;
  private static final int DEBUG_START_TIME = 12;
  private static final int DEBUG_END_TIME = 13;

  @Value("${isDebug}")
  private Boolean isDebug;
  @Value("${love.vcode.sign-name}")
  private String vcodeSignName;

  @Autowired
  private ChildrenInsuranceService childrenInsuranceService;
  @Autowired
  private ChildrenInsuranceRepository childrenInsuranceRepository;
  @Autowired
  private SmsSendService smsSendService;
  @Autowired
  private KaptchaConfig vcodeKaptchaConfig;
  @Autowired
  private KaptchaController kaptchaController;

  private KaptchaCounter vcodeKaptchaCounter;

  @Bean(name = "vcodeKaptchaConfig")
  @ConfigurationProperties(prefix = "love.vcode.kaptcha")
  public KaptchaConfig getVcodeKaptchaConfig() {
    return new KaptchaConfig();
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    vcodeKaptchaCounter = new KaptchaCounter(vcodeKaptchaConfig);
  }

  /**
   * 默认试算
   */
  @RequestMapping(value = "defaultTrial",method = RequestMethod.POST)
  public String defaultTrial() throws OperationFailedException {
    return JsonUtil.toJson(childrenInsuranceService.defaultTrial());
  }

  /**
   * 获取健康告知
   */
  @RequestMapping(value = "healthStatement/{money}",method = RequestMethod.POST)
  public String healthStatement(@PathVariable int money) throws OperationFailedException {
    String geneParamValue = money + "万元";
    return childrenInsuranceService.healthStatement(geneParamValue);
  }


  /**
   * 提交健康告知
   * @param money
   * @param controlValue
   * @return
   * @throws OperationFailedException
   */
  @RequestMapping(value = "submitHealthState/{money}/{controlValue}",method = RequestMethod.POST)
  public String submitHealthState(@PathVariable int money,@PathVariable int controlValue) throws OperationFailedException {
    if(controlValue != 0){
      throw new OperationFailedException("健康告知验证不通过");
    }
    String geneParamValue = money + "万元";
    return JsonUtil.toJson(childrenInsuranceService.submitHealthState(geneParamValue));
  }


  /**
   * 存储投保信息并投保在线支付
   * @param insureReq 投保信息
   */
  @RequestMapping(value = "insure",method = RequestMethod.POST)
  public ChildrenInsurancePayResp insure(@RequestBody InsureReq insureReq) throws OperationFailedException {
    LOGGER.info("InsureReq = "+JsonUtil.toJson(insureReq));
    Set<ConstraintViolation<InsureReq>> constraintViolationSet = VALIDATOR.validate(insureReq);
    if (constraintViolationSet.size() > 0) {
      throw new OperationFailedException(constraintViolationSet.iterator().next().getMessage());
    }

    isTimeToInPay();

    //验证码验证
    smsSendService.verify(insureReq.getMobile(), insureReq.getVcode());
    //存储投保信息并投保
    CommonResult<InsureResp> insureRespCommonResult = childrenInsuranceService.insureAndSave(insureReq.buildChildrenInsuarnce());
    ChildrenInsuarnce childrenInsuarnce = childrenInsuranceRepository.findByInsureNum(insureRespCommonResult.getData().getInsureNum());
    //验证码删除
    smsSendService.delete(insureReq.getMobile());
    //在线支付
    childrenInsuarnce = childrenInsuranceService.tryPay(childrenInsuarnce);
    return new ChildrenInsurancePayResp(childrenInsuarnce.getInsureNum(),childrenInsuarnce.getPayUrl());
  }

  /**
   * 投保时间判断
   *
   * @throws OperationFailedException
   */
  private void isTimeToInPay() throws OperationFailedException {
    Date nowDate = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
    Integer hours = Integer.valueOf(simpleDateFormat.format(nowDate));
    if (hours >= INSURE_HOURS || (isDebug && (hours >= DEBUG_START_TIME) && (hours < DEBUG_END_TIME))) {
      throw new OperationFailedException("900023");
    }
  }


  /**
   * 在线支付
   * @param insureNum 投保单号
   * @return
   * @throws OperationFailedException
   */
  @RequestMapping(value = "tryPay/{insureNum}", method = RequestMethod.POST)
  public ChildrenInsurancePayResp tryPay(@PathVariable String insureNum) throws OperationFailedException {
    isTimeToInPay();
    ChildrenInsuarnce childrenInsuarnce = childrenInsuranceRepository.findByInsureNum(insureNum);
    long nowTime = new Date().getTime();
    //订单是否超时
    if (childrenInsuarnce.getOrderStatus().equals(ChildrenInsuarnce.OrderStatus.EXPIRED) || (nowTime - childrenInsuarnce.getCreateDate().getTime()) / (1000 * 60) > EXPIRED_TIME) {
      throw new OperationFailedException("该订单已失效");
    }
    //payUrl不为空
    if (StringUtils.isNotBlank(childrenInsuarnce.getPayUrl())) {
      return new ChildrenInsurancePayResp(insureNum, childrenInsuarnce.getPayUrl());
    } else {
      //payUrl为空
      return new ChildrenInsurancePayResp(insureNum, childrenInsuranceService.tryPay(childrenInsuarnce).getPayUrl());
    }
  }


  /**
   * 保单地址获取
   *
   * @param insureNum 投保单号
   */
  @RequestMapping(value = "download/{insureNum}")
  public String download(@PathVariable String insureNum) {
    return childrenInsuranceService.download(insureNum);
  }


  /**
   * 支付成功回调
   *
   * @param notifyMap
   * @throws OperationFailedException
   * @throws UnsupportedEncodingException
   */
  @RequestMapping(value = "notify", method = RequestMethod.POST)
  public QixinNotifyResultResp payNotify(@RequestBody Map<String, Object> notifyMap) {
    LOGGER.info("result = " + JsonUtil.toJson(notifyMap));
    try {
      childrenInsuranceService.payNotify(notifyMap);
      return new QixinNotifyResultResp(true);
    } catch (OperationFailedException ofe) {
      return new QixinNotifyResultResp(false, ofe.getMessage());
    }
  }

  /**
   * 验证码发送
   *
   * @param mobileNum 手机号
   * @param uniqueId  openId
   * @param kaptcha   图形验证码
   * @param request
   * @throws OperationFailedException
   */
  @RequestMapping(value = "vcode/send")
  public void vcodeSend(String mobileNum, String uniqueId, String kaptcha, HttpServletRequest request)
      throws OperationFailedException {
    String clientIp = HttpUtil.getIp(request);
    if (StringUtils.isBlank(clientIp)) {
      throw new OperationFailedException("无法识别的客户端");
    }
    if (vcodeKaptchaCounter.isKaptchaRequired(clientIp)) {
      if (StringUtils.isBlank(kaptcha)) {
        throw new OperationFailedException("请输入图片验证码");
      }
      if (kaptchaController.validateKaptcha(VCODE_KAPTCHA_KEY, uniqueId, kaptcha, request)) {
        vcodeKaptchaCounter.reset(clientIp);
      } else {
        throw new OperationFailedException("图片验证码错误");
      }
    }
    smsSendService.vcodeSend(mobileNum, vcodeSignName);
    vcodeKaptchaCounter.add(clientIp);
  }

}
