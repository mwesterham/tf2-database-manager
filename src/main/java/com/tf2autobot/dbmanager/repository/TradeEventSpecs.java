package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.TradeEvent;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.tf2autobot.dbmanager.entity.TradeEventItem;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TradeEventSpecs {

    public static Specification<TradeEvent> withFilters(
            String partner,
            Set<String> partnerNameMatches,
            String offerId,
            Double minProfit,
            Double maxProfit,
            String dateFrom,
            String dateTo,
            String item,
            Set<Integer> itemDefindexes,
            String status) {

        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (partner != null && !partner.isBlank()) {
                Predicate byId = cb.like(root.get("partnerSteamId64"), "%" + partner.trim() + "%");
                if (partnerNameMatches != null && !partnerNameMatches.isEmpty()) {
                    Predicate byName = root.get("partnerSteamId64").in(partnerNameMatches);
                    predicates.add(cb.or(byId, byName));
                } else {
                    predicates.add(byId);
                }
            }

            if (offerId != null && !offerId.isBlank())
                predicates.add(cb.like(root.get("offerId"), "%" + offerId.trim() + "%"));

            if (minProfit != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("realizedProfitInRefined"), minProfit));

            if (maxProfit != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("realizedProfitInRefined"), maxProfit));

            if (dateFrom != null && !dateFrom.isBlank()) {
                Instant from = LocalDate.parse(dateFrom).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("tradedAt"), from));
            }

            if (dateTo != null && !dateTo.isBlank()) {
                Instant to = LocalDate.parse(dateTo).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.lessThan(root.get("tradedAt"), to));
            }

            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("status"), status.trim().toLowerCase()));

            if (item != null && !item.isBlank()) {
                query.distinct(true);
                var itemJoin = root.join("items", jakarta.persistence.criteria.JoinType.INNER);
                var itemOr = new ArrayList<Predicate>();
                itemOr.add(cb.like(cb.lower(itemJoin.get("sku")), "%" + item.trim().toLowerCase() + "%"));
                if (itemDefindexes != null) {
                    for (Integer defindex : itemDefindexes) {
                        itemOr.add(cb.like(itemJoin.get("sku"), defindex + ";%"));
                    }
                }
                predicates.add(cb.or(itemOr.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Orders by realized profit with nulls last (either direction), then newest first. Adds no filter. */
    public static Specification<TradeEvent> orderByRealizedNullsLast(boolean ascending) {
        return (root, query, cb) -> {
            var realized = root.<Double>get("realizedProfitInRefined");
            var nullsLast = cb.selectCase().when(cb.isNull(realized), 1).otherwise(0);
            query.orderBy(
                    cb.asc(nullsLast),
                    ascending ? cb.asc(realized) : cb.desc(realized),
                    cb.desc(root.get("id")));
            return null;
        };
    }
}
