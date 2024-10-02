package com.example.quitsmokingapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class QuitPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private LocalDateTime quitDateTime;
    private int cigarettesPerDay;
    private double pricePerPack;
    private double moneyGoal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getQuitDateTime() {
        return quitDateTime;
    }

    public void setQuitDateTime(LocalDateTime quitDateTime) {
        this.quitDateTime = quitDateTime;
    }

    public int getCigarettesPerDay() {
        return cigarettesPerDay;
    }

    public void setCigarettesPerDay(int cigarettesPerDay) {
        this.cigarettesPerDay = cigarettesPerDay;
    }

    public double getPricePerPack() {
        return pricePerPack;
    }

    public void setPricePerPack(double pricePerPack) {
        this.pricePerPack = pricePerPack;
    }

    public double getMoneyGoal() {
        return moneyGoal;
    }

    public void setMoneyGoal(double moneyGoal) {
        this.moneyGoal = moneyGoal;
    }

    public long getHoursWithoutSmoking() {
        return ChronoUnit.HOURS.between(quitDateTime, LocalDateTime.now());
    }

    public double getMoneySaved() {
        return getHoursWithoutSmoking() * cigarettesPerDay * pricePerPack / (20.0 * 24);
    }

    public boolean hasReachedMoneyGoal() {
        return getMoneySaved() >= moneyGoal;
    }
}