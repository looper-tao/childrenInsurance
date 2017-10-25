package com.newtank.libra.children.repository;

import com.newtank.libra.children.entity.SMSProvider;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by looper on 2017/9/27.
 */
public interface SMSProviderRepository extends CrudRepository<SMSProvider, Long>, JpaSpecificationExecutor<SMSProvider> {

  List<SMSProvider> findByActiveTrueOrderByRankDesc();

  List<SMSProvider> findByIdIn(long[] ids);

  List<SMSProvider> findByRankBetween(long minRank, long maxRank);

}
