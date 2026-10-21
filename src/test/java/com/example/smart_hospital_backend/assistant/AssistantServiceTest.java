package com.example.smart_hospital_backend.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class AssistantServiceTest {
    @Test
    void returnsRuleBasedAnswerWhenEnabled() {
        RuleBasedFaqAssistant faq = mock(RuleBasedFaqAssistant.class);
        when(faq.answer("How do I register?")).thenReturn("Use registration.");
        AssistantService service = new AssistantService(faq, true);

        AssistantResponse response = service.chat(new AssistantRequest("How do I register?"));

        assertEquals("Use registration.", response.reply());
        assertEquals("RULE_BASED", response.mode());
    }

    @Test
    void disabledAssistantFailsAsUnavailable() {
        AssistantService service = new AssistantService(new RuleBasedFaqAssistant(), false);

        assertThrows(AssistantUnavailableException.class,
                () -> service.chat(new AssistantRequest("How do I register?")));
    }
}
