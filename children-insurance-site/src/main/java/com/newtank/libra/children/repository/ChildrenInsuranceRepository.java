package com.newtank.libra.children.repository;

import com.newtank.libra.children.entity.ChildrenInsuarnce;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by looper on 2017/10/26.
 */
@Repository
public interface ChildrenInsuranceRepository extends CrudRepository<ChildrenInsuarnce, Long>, JpaSpecificationExecutor<ChildrenInsuarnce> {
  @Transactional
  @Modifying
  @Query(value = "UPDATE t_libra_children_insurance t SET t.order_status='EXPIRED' where t.order_status in ('WAIT_PAY','REFUSED') and (SELECT round((UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(t.create_date))/60)) > 15",nativeQuery = true)
  void updateExpiredOrder();

  ChildrenInsuarnce findByInsureNum(String insureNum);

  List<ChildrenInsuarnce> findByMobile(String mobile);
}
