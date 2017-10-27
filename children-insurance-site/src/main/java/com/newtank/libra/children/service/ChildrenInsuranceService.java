package com.newtank.libra.children.service;

import com.huize.qixin.api.model.info.*;
import com.huize.qixin.api.req.health.HealthStatementReq;
import com.huize.qixin.api.req.health.SubmitHealthStateReq;
import com.huize.qixin.api.req.insure.InsureReq;
import com.huize.qixin.api.req.pay.OnlinePayReq;
import com.huize.qixin.api.req.policy.PolicyUrlReq;
import com.huize.qixin.api.req.trial.DefaultTrialReq;
import com.huize.qixin.api.resp.health.HealthStatementResp;
import com.huize.qixin.api.resp.health.SubmitHealthStateResp;
import com.huize.qixin.api.resp.insure.InsureResp;
import com.huize.qixin.api.resp.pay.OnlinePayResp;
import com.huize.qixin.api.resp.policy.PolicyUrlResp;
import com.huize.qixin.api.resp.trial.TrialResp;
import com.newtank.libra.children.conf.QiXinInsuranceConfig;
import com.newtank.libra.children.entity.ChildrenInsuarnce;
import com.newtank.libra.children.exception.OperationFailedException;
import com.newtank.libra.children.repository.ChildrenInsuranceRepository;
import com.newtank.libra.children.service.qixin.QiXinOpenApiService;
import com.newtank.libra.children.utils.JsonUtil;
import com.qixin.openapi.model.common.CommonResult;
import com.qixin.openapi.util.Md5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by looper on 2017/10/25.
 */
@Service
public class ChildrenInsuranceService extends QiXinOpenApiService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChildrenInsuranceService.class);
  @Autowired
  private QiXinInsuranceConfig childrenInsuranceConfig;
  @Autowired
  private ChildrenInsuranceRepository childrenInsuranceRepository;

  private static final int HEALTHYID = 274;
  private static final int MODULEID = 157;
  private static final int ANSWERID = 509;
  private static final String PROTECTITEMID = "2383";
  private static final int RESPCODE = 0;

  private static Map<Integer, Integer> QUESTIONS = new HashMap<>();

  static {
    for (int questionId = 953; questionId <= 960; questionId++) {
      QUESTIONS.put(questionId, questionId - 952);
    }
  }

  @Value("${isDebug}")
  private Boolean isDebug;

  @Bean(name = "childrenInsuranceConfig")
  @ConfigurationProperties(prefix = "huize.qixin.children")
  public QiXinInsuranceConfig childrenInsuranceConfig() {
    return new QiXinInsuranceConfig();
  }

  /**
   * 默认试算
   */
  public CommonResult<TrialResp> defaultTrial() throws OperationFailedException {
    DefaultTrialReq defaultTrial = initBaseReq(DefaultTrialReq.class);
    defaultTrial.setCaseCode(childrenInsuranceConfig.getCaseCode());

    CommonResult<TrialResp> trialRespCommonResult = operation.defaultTrial(defaultTrial);
    LOGGER.info("trialRespCommonResult = " + JsonUtil.toJson(trialRespCommonResult));

    if (RESPCODE != trialRespCommonResult.getRespCode() || null == trialRespCommonResult.getData()) {
      throw new OperationFailedException("默认试算失败!");
    }
    return trialRespCommonResult;
  }


  /**
   * 获取健康告知
   */
  public String healthStatement(String geneParamValue) throws OperationFailedException {
    HealthStatementReq healthStatementReq = initBaseReq(HealthStatementReq.class);
    healthStatementReq.setCaseCode(childrenInsuranceConfig.getCaseCode());

    List<GeneParam> geneParamList = new ArrayList<>();
    GeneParam geneParam = getGeneParam(geneParamValue);
    geneParamList.add(geneParam);

    healthStatementReq.setGenes(geneParamList);

    CommonResult<HealthStatementResp> healthStatementRespCommonResult = operation.healthStatement(healthStatementReq);
    LOGGER.info("healthStatementRespCommonResult = " + JsonUtil.toJson(healthStatementRespCommonResult));

    if (RESPCODE != healthStatementRespCommonResult.getRespCode() || null == healthStatementRespCommonResult.getData()) {
      throw new OperationFailedException("获取健康告知失败!");
    }

    return JsonUtil.toJson(healthStatementRespCommonResult);
  }

  /**
   * 选定的试算因子
   *
   * @param geneParamValue 试算因子值
   * @return
   */
  private GeneParam getGeneParam(String geneParamValue) {
    GeneParam geneParam = new GeneParam();
    geneParam.setProtectItemId(PROTECTITEMID);
    geneParam.setSort(2);
    geneParam.setValue(geneParamValue);
    return geneParam;
  }


  /**
   * 提交健康告知
   *
   * @param geneParamValue
   */
  public CommonResult<SubmitHealthStateResp> submitHealthState(String geneParamValue) throws OperationFailedException {
    SubmitHealthStateReq submitHealthStateReq = initBaseReq(SubmitHealthStateReq.class);
    //方案代码
    submitHealthStateReq.setCaseCode(childrenInsuranceConfig.getCaseCode());
    //当前试算因子
    List<GeneParam> geneParamList = new ArrayList<>();
    GeneParam geneParam = getGeneParam(geneParamValue);
    geneParamList.add(geneParam);

    submitHealthStateReq.setGenes(geneParamList);

    HealthyQa healthyQa = new HealthyQa();
    healthyQa.setHealthyId(HEALTHYID);

    List<HealthyQaModule> healthyQaModuleList = new ArrayList<>();
    HealthyQaModule healthyQaModule = new HealthyQaModule();
    healthyQaModule.setModuleId(MODULEID);

    List<HealthyQaQuestion> healthyQaQuestionList = new ArrayList<>();
    Set<Integer> questionSet = QUESTIONS.keySet();

    for (Iterator<Integer> iterator = questionSet.iterator(); iterator.hasNext(); ) {
      Integer questionId = iterator.next();
      HealthyQaQuestion healthyQaQuestion = new HealthyQaQuestion();
      healthyQaQuestion.setQuestionId(questionId);
      healthyQaQuestion.setParentId(0);
      healthyQaQuestion.setQuestionSort(QUESTIONS.get(questionId).byteValue());

      List<HealthyQaAnswer> healthyQaAnswerList = new ArrayList<>();

      HealthyQaAnswer healthyQaAnswer = new HealthyQaAnswer();
      healthyQaAnswer.setAnswerId(ANSWERID);
      healthyQaAnswer.setAnswerValue("0");
      healthyQaAnswer.setKeyCode("insured_isOrNot");
      healthyQaAnswerList.add(healthyQaAnswer);

      //答案信息列表
      healthyQaQuestion.setHealthyQaAnswers(healthyQaAnswerList);
      healthyQaQuestionList.add(healthyQaQuestion);
    }

    //题目信息列表
    healthyQaModule.setHealthyQaQuestions(healthyQaQuestionList);
    healthyQaModuleList.add(healthyQaModule);
    //告知模块信息列表
    healthyQa.setHealthyQaModules(healthyQaModuleList);

    //健康告知答案信息
    submitHealthStateReq.setQaAnswer(healthyQa);

    CommonResult<SubmitHealthStateResp> submitHealthStateRespCommonResult = operation.submitHealthState(submitHealthStateReq);
    LOGGER.info("submitHealthStateRespCommonResult = " + JsonUtil.toJson(submitHealthStateRespCommonResult));

    if (RESPCODE != submitHealthStateRespCommonResult.getRespCode() || null == submitHealthStateRespCommonResult.getData()) {
      throw new OperationFailedException("提交健康告知失败!");
    }

    return submitHealthStateRespCommonResult;


  }

  /**
   * 存储投保信息
   *
   * @param childrenInsuarnce 投保信息
   */
  private ChildrenInsuarnce save(ChildrenInsuarnce childrenInsuarnce, Long totalprice) {
    if (isDebug) {
      //测试服
      childrenInsuarnce.setPayPrice(1);
    } else {
      //正式服
      childrenInsuarnce.setPayPrice(childrenInsuarnce.getTotalPrice());
    }
    //订单创建时间
    childrenInsuarnce.setCreateDate(new Date());
    //订单支付金额
    childrenInsuarnce.setTotalPrice(totalprice);
    //订单状态
    childrenInsuarnce.setOrderStatus(ChildrenInsuarnce.OrderStatus.WAIT_PAY);
    return childrenInsuranceRepository.save(childrenInsuarnce);
  }

  /**
   * 投保
   *
   * @param childrenInsuarnce 投保信息
   */
  private CommonResult<InsureResp> insure(ChildrenInsuarnce childrenInsuarnce) throws OperationFailedException {
    InsureReq insureReq = initBaseReq(InsureReq.class);
    insureReq.setCaseCode(childrenInsuranceConfig.getCaseCode());
    insureReq.setStartDate(childrenInsuarnce.getStartDate());

    //投保人信息
    Applicant applicant = new Applicant();
    applicant.setcName(childrenInsuarnce.getApplicantName());
    applicant.setCardType(childrenInsuarnce.getApplicantCardType().getType());
    applicant.setCardCode(childrenInsuarnce.getApplicantCardCode());
    applicant.setSex(childrenInsuarnce.getRelation().getSex());
    applicant.setBirthday(childrenInsuarnce.getApplicantBirthday());
    applicant.setMobile(childrenInsuarnce.getMobile());
    applicant.setEmail(childrenInsuarnce.getEmail());
    insureReq.setApplicant(applicant);

    //被保人信息列表
    List<Insurant> insurantList = new ArrayList<>();
    //被保人信息
    Insurant insurant = new Insurant();
    insurant.setInsurantId(String.valueOf(childrenInsuarnce.getId()));
    insurant.setcName(childrenInsuarnce.getInsurantName());
    insurant.setCardType(childrenInsuarnce.getInsurantCardType().getType());
    insurant.setCardCode(childrenInsuarnce.getInsurantCardCode());
    insurant.setBirthday(childrenInsuarnce.getInsurantBirthday());
    insurant.setSex(childrenInsuarnce.getInsurantSex());
    insurant.setRelationId(childrenInsuarnce.getRelation().getRelation());
    insurant.setCount(1);

    insurant.setSinglePrice(childrenInsuarnce.getTotalPrice());
    insurantList.add(insurant);

    insureReq.setInsurants(insurantList);

    //默认试算
    CommonResult<TrialResp> trialRespCommonResult = defaultTrial();
    //产品试算信息
    String priceArgs = JsonUtil.toJson(trialRespCommonResult.getData().getPriceArgs());
    insureReq.setPriceArgs(priceArgs);

    OtherInfo otherInfo = new OtherInfo();
    CommonResult<SubmitHealthStateResp> submitHealthStateRespCommonResult = submitHealthState(childrenInsuarnce.getGenes());
    otherInfo.setHealthAnswerId((int) submitHealthStateRespCommonResult.getData().getHealthId());
    insureReq.setOtherInfo(otherInfo);
    CommonResult<InsureResp> insureRespCommonResult = operation.insure(insureReq);
    LOGGER.info("insureRespCommonResult = " + JsonUtil.toJson(insureRespCommonResult));

    if (RESPCODE != insureRespCommonResult.getRespCode() || null == insureRespCommonResult.getData()) {
      throw new OperationFailedException("投保失败");
    }

    return insureRespCommonResult;
  }


  /**
   * 投保并存储投保信息
   *
   * @param childrenInsuarnce
   */
  @Transactional
  public CommonResult<InsureResp> insureAndSave(ChildrenInsuarnce childrenInsuarnce, Long totalprice) throws OperationFailedException {
    //存储投保信息
    childrenInsuarnce = save(childrenInsuarnce, totalprice);
    //调用慧泽SDK,进行投保
    CommonResult<InsureResp> insureRespCommonResult = insure(childrenInsuarnce);
    if (null == insureRespCommonResult.getData() || null == insureRespCommonResult.getData().getInsureNum()) {
      throw new OperationFailedException("投保失败");
    }
    //添加投保单号
    childrenInsuarnce.setInsureNum(insureRespCommonResult.getData().getInsureNum());
    childrenInsuranceRepository.save(childrenInsuarnce);
    return insureRespCommonResult;
  }


  /**
   * 在线支付
   *
   * @param childrenInsuarnce 投保信息
   */
  public ChildrenInsuarnce tryPay(ChildrenInsuarnce childrenInsuarnce) throws OperationFailedException {
    OnlinePayReq onlinePayReq = initBaseReq(OnlinePayReq.class);

    //投保单号
    onlinePayReq.setInsureNums(childrenInsuarnce.getInsureNum());
    //订单支付总金额（单位：分）
    if (isDebug) {
      onlinePayReq.setMoney(1);
    } else {
      onlinePayReq.setMoney(childrenInsuarnce.getPayPrice());
    }

    //支付类型
    onlinePayReq.setGateway(ChildrenInsuarnce.PayWay.WXPAY.getWay());
    //客户端类型 2：H5
    onlinePayReq.setClientType(2);
    //支付成功回调（跳转）地址
    onlinePayReq.setCallBackUrl(childrenInsuranceConfig.getCallbackUrl() + childrenInsuarnce.getInsureNum());

    CommonResult<OnlinePayResp> onlinePayRespCommonResult = operation.onlinePay(onlinePayReq);
    LOGGER.info("onlinePayRespCommonResult = " + JsonUtil.toJson(onlinePayRespCommonResult));

    if (RESPCODE != onlinePayRespCommonResult.getRespCode() || null == onlinePayRespCommonResult.getData() || null == onlinePayRespCommonResult.getData().getGatewayUrl()) {
      throw new OperationFailedException(onlinePayRespCommonResult.getRespMsg());
    }
    //更新投保信息 增加支付链接
    childrenInsuarnce.setPayUrl(onlinePayRespCommonResult.getData().getGatewayUrl());

    return childrenInsuranceRepository.save(childrenInsuarnce);
  }

  /**
   * 保单地址查询
   *
   * @param insureNum 投保单号
   * @return
   */
  public String download(String insureNum) throws OperationFailedException {
    PolicyUrlReq policyUrlReq = initBaseReq(PolicyUrlReq.class);
    policyUrlReq.setInsureNum(insureNum);

    CommonResult<PolicyUrlResp> policyUrlRespCommonResult = operation.downloadUrl(policyUrlReq);
    LOGGER.info("policyUrlRespCommonResult = " + JsonUtil.toJson(policyUrlRespCommonResult));

    if (RESPCODE != policyUrlRespCommonResult.getRespCode()) {
      throw new OperationFailedException("保单地址查询失败");
    }

    return JsonUtil.toJson(policyUrlRespCommonResult);
  }


  /**
   * 支付回调接口
   *
   * @param notifyMap
   * @throws OperationFailedException
   */
  @SuppressWarnings("unchecked")
  public void payNotify(Map<String, Object> notifyMap) throws OperationFailedException {
    int notifyType = (int) notifyMap.get("notifyType");
    if (notifyType == 2 || notifyType == 3 || notifyType == 9) {
      //签名
      String sign = (String) notifyMap.get("sign");
      //支付(2)/出单(3)/可以生成保单(9)通知信息
      Map<String, Object> dataMap = (Map<String, Object>) notifyMap.get("data");
      //是否成功 true or false
      boolean state = (boolean) dataMap.get("state");
      if (sign == null || !sign.equals(Md5Utils.getUtf8MD5String(config.getSignKey() + JsonUtil.toJson(dataMap))) || !state) {
        throw new OperationFailedException("签名错误");
      }
      //    int partnerId = (int) dataMap.get("partnerId");
      String insureNum = (String) dataMap.get("insureNum");
      //通过保单号 查找该订单
      ChildrenInsuarnce childrenInsuarnce = childrenInsuranceRepository.findByInsureNum(insureNum);
      LOGGER.info("childrenInsuarnce = " + JsonUtil.toJson(childrenInsuarnce));
      if (childrenInsuarnce == null) {
        throw new OperationFailedException("该投保单号不存在");
      }
      if (notifyType == 2 || notifyType == 3) {
        //支付回调时 需要匹配金额是否一致
        if (notifyType == 2) {
          Integer price = (Integer) dataMap.get("price");
          if (childrenInsuarnce.getPayPrice() == price) {
            throw new OperationFailedException("订单金额不一致");
          }
          LOGGER.info("result = 支付成功");
        }

        //支付成功,添加支付时间
        childrenInsuarnce.setOrderStatus(ChildrenInsuarnce.OrderStatus.PAYED);
        childrenInsuarnce.setPayTime(new Date());
        childrenInsuranceRepository.save(childrenInsuarnce);
      } else if (notifyType == 9) {
        //可以生成保单
        childrenInsuarnce.setIsDownload(true);
        childrenInsuranceRepository.save(childrenInsuarnce);
      }

    }

  }


}
