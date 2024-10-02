package com.example.quitsmokingapp.controller;

import com.example.quitsmokingapp.model.QuitPlan;
import com.example.quitsmokingapp.service.QuitPlanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Controller
public class QuitPlanController {

    @Autowired
    private QuitPlanService quitPlanService;

    @GetMapping("/quit-plan")
    public String quitPlan(Model model) {
        QuitPlan quitPlan = quitPlanService.getCurrentUserQuitPlan();
        model.addAttribute("quitPlan", quitPlan);
        if (quitPlan != null) {
            model.addAttribute("moneySaved", quitPlanService.calculateMoneySaved(quitPlan));
            model.addAttribute("moneyGoal", quitPlan.getMoneyGoal());
            model.addAttribute("goalReached", quitPlan.hasReachedMoneyGoal());
        } else {
            model.addAttribute("noQuitPlan", true);
        }
        return "quit-plan";
    }

    @PostMapping("/save-quit-plan")
    public String saveQuitPlan(@RequestParam String quitDateTime,
                               @RequestParam int cigarettesPerDay,
                               @RequestParam double pricePerPack,
                               @RequestParam double moneyGoal,
                               Model model) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withZone(ZoneOffset.UTC);
            LocalDateTime parsedDateTime = LocalDateTime.parse(quitDateTime, formatter);
            quitPlanService.saveQuitPlan(parsedDateTime, cigarettesPerDay, pricePerPack, moneyGoal);
            return "redirect:/quit-plan";
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid date/time format. Use yyyy-MM-dd'T'HH:mm");
            return "quit-plan";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Error saving quit plan: " + e.getMessage());
            return "quit-plan";
        }
    }

    @GetMapping("/quit-progress")
    public String quitProgress(Model model) {
        QuitPlan quitPlan = quitPlanService.getCurrentUserQuitPlan();
        if (quitPlan == null || quitPlan.getQuitDateTime() == null) {
            return "redirect:/quit-plan";
        }

        LocalDateTime quitDateTimeUTC = quitPlan.getQuitDateTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        long timeSinceQuitMs = System.currentTimeMillis() - quitDateTimeUTC.toInstant(ZoneOffset.UTC).toEpochMilli();

        List<Milestone> milestones = Arrays.asList(
                new Milestone("Pulse rate", 20 * 60 * 1000L, "20 minutes"),
                new Milestone("Oxygen levels", 8 * 60 * 60 * 1000L, "8 hours"),
                new Milestone("Carbon monoxide eliminated", 24 * 60 * 60 * 1000L, "24 hours"),
                new Milestone("Nicotine level", 2 * 24 * 60 * 60 * 1000L, "2 days"),
                new Milestone("Taste and smell", 3 * 24 * 60 * 60 * 1000L, "3 days"),
                new Milestone("Breathing", 3 * 24 * 60 * 60 * 1000L, "3 days"),
                new Milestone("Energy", 4 * 24 * 60 * 60 * 1000L, "4 days"),
                new Milestone("Blood circulation", 90L * 24 * 60 * 60 * 1000L, "3 months"),
                new Milestone("Heart disease risk", 365L * 24 * 60 * 60 * 1000L, "1 year"),
                new Milestone("Lung cancer risk", 10L * 365 * 24 * 60 * 60 * 1000L, "10 years")
        );

        milestones.forEach(m -> m.calculateProgress(timeSinceQuitMs));

        ObjectMapper objectMapper = new ObjectMapper();
        String milestonesJson;
        try {
            milestonesJson = objectMapper.writeValueAsString(milestones);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing milestones", e);
        }

        model.addAttribute("quitPlan", quitPlan);
        model.addAttribute("quitDateTime", quitDateTimeUTC);
        model.addAttribute("hoursWithoutSmoking", timeSinceQuitMs / (1000 * 60 * 60));
        model.addAttribute("moneySaved", quitPlanService.calculateMoneySaved(quitPlan));
        model.addAttribute("moneyGoal", quitPlan.getMoneyGoal());
        model.addAttribute("goalReached", quitPlan.hasReachedMoneyGoal());
        model.addAttribute("milestonesJson", milestonesJson);
        model.addAttribute("milestones", milestones);

        return "quit-progress";
    }

    public static class Milestone {
        private String name;
        private long durationMs;
        private String duration;
        private double progress;

        public Milestone(String name, long durationMs, String duration) {
            this.name = name;
            this.durationMs = durationMs;
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }

        public void calculateProgress(long timeSinceQuitMs) {
            this.progress = Math.min(100, (double) timeSinceQuitMs / durationMs * 100);
        }
    }
}