package com.example.quitsmokingapp.service;

import com.example.quitsmokingapp.model.QuitPlan;
import com.example.quitsmokingapp.model.User;
import com.example.quitsmokingapp.repository.QuitPlanRepository;
import com.example.quitsmokingapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QuitPlanService {

    private final QuitPlanRepository quitPlanRepository;
    private final UserRepository userRepository;

    public QuitPlanService(QuitPlanRepository quitPlanRepository, UserRepository userRepository) {
        this.quitPlanRepository = quitPlanRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuitPlan getCurrentUserQuitPlan() {
        String username = getCurrentUsername();
        return quitPlanRepository.findByUserUsername(username)
                .orElse(null); // Return null if no QuitPlan is found
    }

    @Transactional
    public void saveQuitPlan(LocalDateTime quitDateTime, int cigarettesPerDay, double pricePerCigarette, double moneyGoal) { // Updated to include moneyGoal
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuitPlan quitPlan = quitPlanRepository.findByUserUsername(username)
                .orElse(new QuitPlan()); // Create a new QuitPlan if not found

        quitPlan.setQuitDateTime(quitDateTime);
        quitPlan.setCigarettesPerDay(cigarettesPerDay);
        quitPlan.setPricePerPack(pricePerCigarette);
        quitPlan.setMoneyGoal(moneyGoal); // Set the moneyGoal
        quitPlan.setUser(user);

        quitPlanRepository.save(quitPlan);
    }

    public long calculateHoursWithoutSmoking(QuitPlan quitPlan) {
        return quitPlan.getHoursWithoutSmoking();
    }

    public double calculateMoneySaved(QuitPlan quitPlan) {
        return quitPlan.getMoneySaved();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}