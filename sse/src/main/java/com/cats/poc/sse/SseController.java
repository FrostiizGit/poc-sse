package com.cats.poc.sse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SseController {
    // This list could be used to send events to all connected clients
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    
    @GetMapping("/")
    public String ping() {
        return "PoC SSE Spring Boot";
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Timeout set to Long.MAX_VALUE for demonstration

        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        // Example of sending an initial event on connection
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connection Established"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Scheduled(fixedRate = 5000)
    public void sendPingMessage() {
        System.out.println("sendPingMessage");
        this.emitters.forEach((emitter) -> {
            System.out.println("Looping through the emitters" + emitter);
            try {
                emitter.send(SseEmitter.event().name("ping").data("Ping -> " + new Date()));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
    }
}