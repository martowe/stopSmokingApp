package com.example.quitsmokingapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TipsController {

    @GetMapping("/quit-tips")
    public String quitTips() {
        return "quit-tips";
    }
}