package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.PricelistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricelistRepository extends JpaRepository<PricelistEntry, String> {
    List<PricelistEntry> findAllByEnabled(boolean enabled);
    List<PricelistEntry> findAllByIntent(int intent);
    List<PricelistEntry> findAllByAutoManaged(boolean autoManaged);
}
