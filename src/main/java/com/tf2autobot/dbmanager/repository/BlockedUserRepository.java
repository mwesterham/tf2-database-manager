package com.tf2autobot.dbmanager.repository;

import com.tf2autobot.dbmanager.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {
}
