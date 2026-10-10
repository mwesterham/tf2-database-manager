package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradeEventRepository extends JpaRepository<TradeEvent, Long>, JpaSpecificationExecutor<TradeEvent> {

    boolean existsByOfferId(String offerId);

    @Query("SELECT SUM(e.profitInRefined) FROM TradeEvent e")
    Optional<Double> sumProfit();

    @Query(value = """
            SELECT DATE(traded_at) AS trade_date,
                   SUM(profit_in_refined) AS profit,
                   COUNT(*) AS trade_count
            FROM trade_events
            WHERE traded_at >= NOW() - INTERVAL '30 days'
            GROUP BY trade_date
            ORDER BY trade_date ASC
            """, nativeQuery = true)
    List<Object[]> findDailyProfit();
}
