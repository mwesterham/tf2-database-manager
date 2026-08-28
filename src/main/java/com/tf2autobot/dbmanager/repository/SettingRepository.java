package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
}
