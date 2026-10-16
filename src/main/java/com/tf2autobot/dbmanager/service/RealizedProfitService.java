package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.RealizedDto;
import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.entity.TradeEventItem;
import com.tf2autobot.dbmanager.entity.TradeRealizedPair;
import com.tf2autobot.dbmanager.repository.TradeEventRepository;
import com.tf2autobot.dbmanager.repository.TradeRealizedPairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pairs each unit the bot sold with the earliest earlier unit of the same SKU it bought (FIFO),
 * valued at the prices the trades used (the bot's sell/buy price then), and stores the pairs plus
 * each selling trade's realized profit. Rebuilt from scratch at startup and after every accepted
 * trade, so the result only depends on stored history.
 *
 * Currency isn't paired: metal always counts as currency, and keys do in any trade with a non-key,
 * non-metal item (matching tf2autobot-java's TradeEvaluator). Pure key/metal trades pair keys.
 */
@Service
public class RealizedProfitService {

    private static final Logger log = LoggerFactory.getLogger(RealizedProfitService.class);
    private static final Set<String> METAL = Set.of("5000;6", "5001;6", "5002;6");
    private static final String KEY = "5021;6";

    private final TradeEventRepository eventRepo;
    private final TradeRealizedPairRepository pairRepo;

    public RealizedProfitService(TradeEventRepository eventRepo, TradeRealizedPairRepository pairRepo) {
        this.eventRepo = eventRepo;
        this.pairRepo = pairRepo;
    }

    // Transactional itself: calling rebuild() from here bypasses the proxy, so its annotation wouldn't apply
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void rebuildOnStartup() {
        rebuild();
    }

    @Transactional
    public void rebuild() {
        List<TradeEvent> trades = eventRepo.findAcceptedWithItemsSince(TradeEventService.DIRECTION_FIX_AT);
        List<TradeRealizedPair> pairs = match(trades);

        Map<Long, Double> realized = new HashMap<>();
        for (TradeRealizedPair p : pairs) {
            realized.merge(p.getSellTradeEventId(), p.getRevenueRefined() - p.getCostRefined(), Double::sum);
        }
        for (TradeEvent t : trades) {
            t.setRealizedProfitInRefined(realized.get(t.getId()));
        }
        pairRepo.deleteAllInBatch();
        pairRepo.saveAll(pairs);
        log.info("Realized profit rebuilt: {} trades, {} pairs", trades.size(), pairs.size());
    }

    /** FIFO pairing over trades in chronological order. Pure: no persistence. */
    static List<TradeRealizedPair> match(List<TradeEvent> tradesInOrder) {
        Map<String, Deque<TradeEventItem>> lots = new HashMap<>();
        List<TradeRealizedPair> pairs = new ArrayList<>();
        for (TradeEvent trade : tradesInOrder) {
            List<TradeEventItem> items = tradeItems(trade);
            // Sales first, so a unit can only pair with a purchase from an earlier trade
            for (TradeEventItem item : items) {
                if (!"given".equals(item.getDirection())) continue;
                Deque<TradeEventItem> queue = lots.get(item.getSku());
                TradeEventItem bought = queue != null ? queue.pollFirst() : null;
                if (bought == null) continue;
                TradeRealizedPair p = new TradeRealizedPair();
                p.setSku(item.getSku());
                p.setBuyTradeEventId(bought.getTradeEvent().getId());
                p.setBuyItemId(bought.getId());
                p.setSellTradeEventId(trade.getId());
                p.setSellItemId(item.getId());
                p.setCostRefined(bought.getUsedPriceRefined());
                p.setRevenueRefined(item.getUsedPriceRefined());
                pairs.add(p);
            }
            for (TradeEventItem item : items) {
                if ("received".equals(item.getDirection())) {
                    lots.computeIfAbsent(item.getSku(), k -> new ArrayDeque<>()).addLast(item);
                }
            }
        }
        return pairs;
    }

    /** Non-currency items of a trade, in item order. */
    static List<TradeEventItem> tradeItems(TradeEvent trade) {
        boolean keysAreCurrency = trade.getItems().stream()
                .anyMatch(i -> !METAL.contains(i.getSku()) && !KEY.equals(i.getSku()));
        return trade.getItems().stream()
                .filter(i -> !METAL.contains(i.getSku()) && !(keysAreCurrency && KEY.equals(i.getSku())))
                .sorted(Comparator.comparing(TradeEventItem::getId))
                .toList();
    }

    /** Per-trade realized detail for the given trades (one query per direction). */
    @Transactional(readOnly = true)
    public Map<Long, RealizedDto> describe(Collection<TradeEvent> trades) {
        Set<Long> ids = new HashSet<>();
        for (TradeEvent t : trades) ids.add(t.getId());
        List<TradeRealizedPair> asSell = ids.isEmpty() ? List.of() : pairRepo.findBySellTradeEventIdIn(ids);
        List<TradeRealizedPair> asBuy = ids.isEmpty() ? List.of() : pairRepo.findByBuyTradeEventIdIn(ids);
        Map<Long, TradeRealizedPair> bySellItem = new HashMap<>();
        Map<Long, TradeRealizedPair> byBuyItem = new HashMap<>();
        asSell.forEach(p -> bySellItem.put(p.getSellItemId(), p));
        asBuy.forEach(p -> byBuyItem.put(p.getBuyItemId(), p));

        Map<Long, RealizedDto> out = new HashMap<>();
        for (TradeEvent t : trades) {
            boolean tracked = "accepted".equals(t.getStatus()) && t.getTradedAt() != null
                    && !t.getTradedAt().isBefore(TradeEventService.DIRECTION_FIX_AT);
            if (!tracked) {
                out.put(t.getId(), RealizedDto.untracked());
                continue;
            }
            List<RealizedDto.Sold> sold = new ArrayList<>();
            List<String> unpaired = new ArrayList<>();
            List<RealizedDto.Bought> bought = new ArrayList<>();
            for (TradeEventItem item : tradeItems(t)) {
                if ("given".equals(item.getDirection())) {
                    TradeRealizedPair p = bySellItem.get(item.getId());
                    if (p != null) sold.add(new RealizedDto.Sold(item.getSku(), p.getRevenueRefined(), p.getCostRefined(), p.getBuyTradeEventId()));
                    else unpaired.add(item.getSku());
                } else {
                    TradeRealizedPair p = byBuyItem.get(item.getId());
                    bought.add(new RealizedDto.Bought(item.getSku(), item.getUsedPriceRefined(),
                            p != null ? p.getSellTradeEventId() : null, p != null ? p.getRevenueRefined() : null));
                }
            }
            out.put(t.getId(), new RealizedDto(true, sold, unpaired, bought));
        }
        return out;
    }
}
