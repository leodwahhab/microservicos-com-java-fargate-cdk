package com.example.aws_project01.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger LOG = Logger.getLogger(TestController.class.getName());

    @GetMapping("/dog/{name}")
    public ResponseEntity<String> dogTest(@PathVariable String name) {
        LOG.info("Test Controller - name: " + name);

        return ResponseEntity.ok("Name: " + name);
    }

    @GetMapping("/dog/color")
    public ResponseEntity<?> dogColor() {
        LOG.info("Test controller - Always black!");

        return ResponseEntity.ok("Always black!");
    }
}
