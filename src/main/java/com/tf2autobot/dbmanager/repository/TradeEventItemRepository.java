package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEventItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TradeEventItemRepository extends JpaRepository<TradeEventItem, Long> {

    @Query(value = """
            SELECT tei.sku, COUNT(DISTINCT tei.trade_event_id) AS trade_count
            FROM trade_event_items tei
            GROUP BY tei.sku
            ORDER BY trade_count DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findMostTradedItems();

    @Query(value = """
            SELECT tei.sku, SUM(te.profit_in_refined) AS total_profit
            FROM trade_event_items tei
            JOIN trade_events te ON tei.trade_event_id = te.id
            WHERE tei.direction = 'received'
            GROUP BY tei.sku
            ORDER BY total_profit DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findMostProfitableItems();
}
