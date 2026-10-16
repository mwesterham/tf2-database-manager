package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeRealizedPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TradeRealizedPairRepository extends JpaRepository<TradeRealizedPair, Long> {

    List<TradeRealizedPair> findBySellTradeEventIdIn(Collection<Long> tradeEventIds);

    List<TradeRealizedPair> findByBuyTradeEventIdIn(Collection<Long> tradeEventIds);
}
