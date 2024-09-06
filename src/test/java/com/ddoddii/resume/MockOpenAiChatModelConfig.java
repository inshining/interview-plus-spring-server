package com.ddoddii.resume;

import org.mockito.Mockito;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockOpenAiChatModelConfig {

    @Bean
    @Primary
    public OpenAiChatClient openAiChatClient() {
        return Mockito.mock(OpenAiChatClient.class);
    }
}
