package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TradeEventRepository extends JpaRepository<TradeEvent, Long> {

    @Query("SELECT SUM(e.profitInRefined) FROM TradeEvent e")
    Optional<Double> sumProfit();
}
