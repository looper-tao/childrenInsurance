package com.newtank.libra.children.task;

import com.newtank.libra.children.repository.ChildrenInsuranceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by looper on 2017/10/9.
 */
@Component
@AllArgsConstructor
public class OrderStatusTask {
  @Autowired
  private ChildrenInsuranceRepository childrenInsuranceRepository;

  @Scheduled(fixedRate =5*60 * 1000)
  public void updateExpiredOrder(){
    childrenInsuranceRepository.updateExpiredOrder();
  }
}
