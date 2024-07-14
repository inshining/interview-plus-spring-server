package com.ddoddii.resume.model.eunm;

import lombok.Getter;

@Getter
public enum LoginType {
    GOOGLE("google"),
    EMAIL("email");

    private String type;

    LoginType(String type) {
        this.type = type;
    }
}
