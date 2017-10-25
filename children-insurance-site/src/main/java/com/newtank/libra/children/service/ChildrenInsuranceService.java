package com.newtank.libra.children.service;

import com.huize.qixin.api.model.info.*;
import com.huize.qixin.api.req.health.HealthStatementReq;
import com.huize.qixin.api.req.health.SubmitHealthStateReq;
import com.huize.qixin.api.req.trial.DefaultTrialReq;
import com.huize.qixin.api.resp.health.HealthStatementResp;
import com.huize.qixin.api.resp.health.SubmitHealthStateResp;
import com.huize.qixin.api.resp.trial.TrialResp;
import com.newtank.libra.children.conf.QiXinInsuranceConfig;
import com.newtank.libra.children.service.qixin.QiXinOpenApiService;
import com.newtank.libra.children.utils.JsonUtil;
import com.qixin.openapi.model.common.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by looper on 2017/10/25.
 */
@Service
public class ChildrenInsuranceService extends QiXinOpenApiService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChildrenInsuranceService.class);
  @Autowired
  private QiXinInsuranceConfig childrenInsuranceConfig;

  @Bean(name = "childrenInsuranceConfig")
  @ConfigurationProperties(prefix = "huize.qixin.love")
  public QiXinInsuranceConfig childrenInsuranceConfig() {
    return new QiXinInsuranceConfig();
  }

  /**
   * 默认试算
   */
  public String defaultTrial() {
    DefaultTrialReq defaultTrial = initBaseReq(DefaultTrialReq.class);
    defaultTrial.setCaseCode(childrenInsuranceConfig.getCaseCode());

    CommonResult<TrialResp> trialRespCommonResult = operation.defaultTrial(defaultTrial);
    LOGGER.info("trialRespCommonResult = " + JsonUtil.toJson(trialRespCommonResult));

    return JsonUtil.toJson(trialRespCommonResult);
  }


  /**
   * 获取健康告知
   */
  public String healthStatement(String geneParamValue) {
    HealthStatementReq healthStatementReq = initBaseReq(HealthStatementReq.class);
    healthStatementReq.setCaseCode(childrenInsuranceConfig.getCaseCode());

    List<GeneParam> geneParamList = new ArrayList<>();
    GeneParam geneParam = new GeneParam();
    geneParam.setProtectItemId("2383");
    geneParam.setSort(2);
    geneParam.setValue(geneParamValue);
    geneParamList.add(geneParam);

    healthStatementReq.setGenes(geneParamList);

    CommonResult<HealthStatementResp> healthStatementRespCommonResult = operation.healthStatement(healthStatementReq);
    LOGGER.info("healthStatementRespCommonResult = " + JsonUtil.toJson(healthStatementRespCommonResult));

    return JsonUtil.toJson(healthStatementRespCommonResult);
  }


  /**
   * 提交健康告知
   *
   * @param geneParamValue
   */
  public String submitHealthState(String geneParamValue) {
    SubmitHealthStateReq submitHealthStateReq = initBaseReq(SubmitHealthStateReq.class);
    //方案代码
    submitHealthStateReq.setCaseCode(childrenInsuranceConfig.getCaseCode());
    //当前试算因子
    List<GeneParam> geneParamList = new ArrayList<>();
    GeneParam geneParam = new GeneParam();
    geneParam.setProtectItemId("2383");
    geneParam.setSort(2);
    geneParam.setValue(geneParamValue);
    geneParamList.add(geneParam);

    submitHealthStateReq.setGenes(geneParamList);

    HealthyQa healthyQa = new HealthyQa();
    healthyQa.setHealthyId(274);

    List<HealthyQaModule> healthyQaModuleList = new ArrayList<>();
    HealthyQaModule healthyQaModule = new HealthyQaModule();
    healthyQaModule.setModuleId(157);

    List<HealthyQaQuestion> healthyQaQuestionList = new ArrayList<>();
    for (int questionId = 953; questionId <= 960; questionId++) {
      HealthyQaQuestion healthyQaQuestion = new HealthyQaQuestion();
      healthyQaQuestion.setQuestionId(questionId);
      healthyQaQuestion.setParentId(0);
      healthyQaQuestion.setQuestionSort((byte) (questionId-952));

      List<HealthyQaAnswer> healthyQaAnswerList = new ArrayList<>();

      HealthyQaAnswer healthyQaAnswer = new HealthyQaAnswer();
      healthyQaAnswer.setAnswerId(509);
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

    LOGGER.info("submitHealthStateRespCommonResult = "+JsonUtil.toJson(submitHealthStateRespCommonResult));

    return JsonUtil.toJson(submitHealthStateRespCommonResult);


  }
}
