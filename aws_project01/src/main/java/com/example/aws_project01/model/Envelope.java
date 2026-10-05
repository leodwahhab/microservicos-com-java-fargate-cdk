package com.example.aws_project01.model;

import com.example.aws_project01.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Envelope {
    private EventType eventType;
    private String data;
}
