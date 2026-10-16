package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TradeEventRepository extends JpaRepository<TradeEvent, Long>, JpaSpecificationExecutor<TradeEvent> {

    boolean existsByOfferId(String offerId);

    @Query("SELECT DISTINCT e FROM TradeEvent e LEFT JOIN FETCH e.items " +
           "WHERE e.status = 'accepted' AND e.tradedAt >= :since ORDER BY e.tradedAt, e.id")
    List<TradeEvent> findAcceptedWithItemsSince(@Param("since") Instant since);

    @Query("SELECT SUM(e.profitInRefined) FROM TradeEvent e")
    Optional<Double> sumProfit();

    @Query(value = """
            SELECT DATE(traded_at) AS trade_date,
                   COALESCE(SUM(realized_profit_refined), 0) AS profit,
                   COUNT(*) AS trade_count
            FROM trade_events
            WHERE traded_at >= NOW() - INTERVAL '30 days'
            GROUP BY trade_date
            ORDER BY trade_date ASC
            """, nativeQuery = true)
    List<Object[]> findDailyProfit();
}
