package com.example.smart_hospital_backend.controller;

import com.example.smart_hospital_backend.dto.QueueEntryResponse;
import com.example.smart_hospital_backend.service.QueueService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
public class QueueController {
    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/check-in/{appointmentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public QueueEntryResponse checkIn(@PathVariable Long appointmentId) {
        return queueService.checkIn(appointmentId);
    }

    @GetMapping("/current")
    public List<QueueEntryResponse> currentQueue(Principal principal) {
        return queueService.currentQueue(principal.getName());
    }

    @GetMapping("/my-status")
    public QueueEntryResponse myStatus(Principal principal) {
        return queueService.myStatus(principal.getName());
    }

    @PatchMapping("/{queueEntryId}/start")
    public QueueEntryResponse start(@PathVariable Long queueEntryId, Principal principal) {
        return queueService.startConsultation(queueEntryId, principal.getName());
    }

    @PatchMapping("/{queueEntryId}/complete")
    public QueueEntryResponse complete(@PathVariable Long queueEntryId, Principal principal) {
        return queueService.completeConsultation(queueEntryId, principal.getName());
    }
}
