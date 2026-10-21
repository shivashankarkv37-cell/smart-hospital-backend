package com.example.smart_hospital_backend.assistant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {
    private final RuleBasedFaqAssistant faqAssistant;
    private final boolean enabled;

    public AssistantService(
            RuleBasedFaqAssistant faqAssistant,
            @Value("${app.assistant.enabled:true}") boolean enabled) {
        this.faqAssistant = faqAssistant;
        this.enabled = enabled;
    }

    public AssistantResponse chat(AssistantRequest request) {
        if (!enabled) {
            throw new AssistantUnavailableException();
        }
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
        if (request.message().length() > 1000) {
            throw new IllegalArgumentException("Message must not exceed 1000 characters");
        }
        return new AssistantResponse(faqAssistant.answer(request.message()), "RULE_BASED");
    }
}
