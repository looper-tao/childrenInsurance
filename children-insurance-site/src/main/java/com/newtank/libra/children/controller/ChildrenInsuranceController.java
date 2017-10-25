package com.newtank.libra.children.controller;

import com.huize.qixin.api.req.insure.InsureReq;
import com.newtank.libra.children.exception.OperationFailedException;
import com.newtank.libra.children.service.ChildrenInsuranceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by looper on 2017/10/25.
 */
@RestController
@RequestMapping("/children/")
public class ChildrenInsuranceController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChildrenInsuranceController.class);
  @Autowired
  private ChildrenInsuranceService childrenInsuranceService;

  /**
   * 默认试算
   */
  @RequestMapping(value = "defaultTrial",method = RequestMethod.POST)
  public String defaultTrial(){
    return childrenInsuranceService.defaultTrial();
  }

  /**
   * 获取健康告知
   */
  @RequestMapping(value = "healthStatement/{money}",method = RequestMethod.POST)
  public String healthStatement(@PathVariable int money){
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
    return childrenInsuranceService.submitHealthState(geneParamValue);
  }


  /**
   * 投保
   * @param insureReq
   */
  @RequestMapping(value = "insure",method = RequestMethod.POST)
  public void insure(@RequestBody InsureReq insureReq){

  }

}
