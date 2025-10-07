package com.berryfi.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Serves the React app for the root path only
     * Let WebConfiguration handle all other routing
     */
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
}
