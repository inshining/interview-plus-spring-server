package com.ddoddii.resume.model.eunm;

import lombok.Getter;

@Getter
public enum InterviewRound {

    FIRST("1차 면접"),
    SECOND("2차 면접"),
    THIRD("3차 면접"),
    FOURTH("4차 면접");
    private String name;

    InterviewRound(String name) {
        this.name = name;
    }

    public static InterviewRound fromString(String name) {
        for (InterviewRound round : InterviewRound.values()) {
            if (round.getName().equals(name)) {
                return round;
            }
        }
        throw new IllegalArgumentException("No enum constant for name: " + name);
    }

}
