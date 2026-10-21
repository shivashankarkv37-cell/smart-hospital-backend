package com.example.smart_hospital_backend.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class AssistantControllerTest {
    @Test
    void returnsReplyAndExplicitRuleBasedMode() {
        AssistantService service = mock(AssistantService.class);
        when(service.chat(new AssistantRequest("How do I book?")))
                .thenReturn(new AssistantResponse("Use the appointments page.", "RULE_BASED"));
        AssistantController controller = new AssistantController(service);

        AssistantResponse response = controller.chat(new AssistantRequest("How do I book?"));

        assertEquals("Use the appointments page.", response.reply());
        assertEquals("RULE_BASED", response.mode());
    }
}
