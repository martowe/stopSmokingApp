package com.example.quitsmokingapp.repository;

import com.example.quitsmokingapp.model.QuitPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuitPlanRepository extends JpaRepository<QuitPlan, Long> {
    Optional<QuitPlan> findByUserId(Long userId);
    Optional<QuitPlan> findByUserUsername(String username);
}