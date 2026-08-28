package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeStatsRepository extends JpaRepository<TradeStats, Integer> {
}
