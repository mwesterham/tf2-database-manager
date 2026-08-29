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

    // Returns SKUs where net received > net given (bot is holding at least one unit)
    @Query(value = """
            SELECT tei.sku
            FROM trade_event_items tei
            GROUP BY tei.sku
            HAVING SUM(CASE WHEN tei.direction = 'received' THEN 1 ELSE -1 END) > 0
            """, nativeQuery = true)
    List<String> findHeldItemSkus();

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
