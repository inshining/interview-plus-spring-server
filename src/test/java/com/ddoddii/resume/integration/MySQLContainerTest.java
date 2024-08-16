package com.ddoddii.resume.integration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MySQLContainerTest {

    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0.39");

    @Test
    void testSimplePutAndGet(){
        assertTrue(MY_SQL_CONTAINER.isRunning());
    }
}
