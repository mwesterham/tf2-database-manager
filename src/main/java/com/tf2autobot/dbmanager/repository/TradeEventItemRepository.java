package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEventItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
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

    // Latest units received per SKU in accepted trades since `since` (one row per unit, newest first)
    @Query(value = """
            SELECT sku, trade_event_id, offer_id, traded_at, used_price_refined
            FROM (
                SELECT tei.sku, te.id AS trade_event_id, te.offer_id, te.traded_at, tei.used_price_refined,
                       ROW_NUMBER() OVER (PARTITION BY tei.sku ORDER BY te.traded_at DESC, tei.id DESC) AS rn
                FROM trade_event_items tei
                JOIN trade_events te ON tei.trade_event_id = te.id
                WHERE tei.direction = 'received'
                  AND te.status = 'accepted'
                  AND te.traded_at >= :since
                  AND tei.sku IN (:skus)
            ) latest
            WHERE rn <= :perSku
            ORDER BY sku, traded_at DESC
            """, nativeQuery = true)
    List<Object[]> findLatestPurchases(@Param("skus") Collection<String> skus,
                                       @Param("since") Instant since,
                                       @Param("perSku") int perSku);

    // Units the bot still holds per SKU: received in accepted trades since `since`, not yet paired
    // with a sale in trade_realized_pairs (FIFO), oldest first. Their price is the bot's cost basis.
    @Query(value = """
            SELECT tei.sku, te.id AS trade_event_id, te.offer_id, te.traded_at, tei.used_price_refined
            FROM trade_event_items tei
            JOIN trade_events te ON tei.trade_event_id = te.id
            WHERE tei.direction = 'received'
              AND te.status = 'accepted'
              AND te.traded_at >= :since
              AND tei.sku IN (:skus)
              AND NOT EXISTS (SELECT 1 FROM trade_realized_pairs p WHERE p.buy_item_id = tei.id)
            ORDER BY tei.sku, te.traded_at, tei.id
            """, nativeQuery = true)
    List<Object[]> findHeldPurchases(@Param("skus") Collection<String> skus, @Param("since") Instant since);

    /** Completed buy→sell pairs sold at or after {@code since}, newest sale first (both trades accepted). */
    @Query(value = """
            SELECT p.sku, p.cost_refined, p.revenue_refined, b.traded_at, s.traded_at,
                   p.buy_trade_event_id, p.sell_trade_event_id
            FROM trade_realized_pairs p
            JOIN trade_events b ON b.id = p.buy_trade_event_id
            JOIN trade_events s ON s.id = p.sell_trade_event_id
            WHERE b.status = 'accepted' AND s.status = 'accepted'
              AND s.traded_at >= :since
            ORDER BY s.traded_at DESC, p.id DESC
            """, nativeQuery = true)
    List<Object[]> findRealizedPairsSince(@Param("since") Instant since);

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
