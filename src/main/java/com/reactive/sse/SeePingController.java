package com.reactive.sse;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(value = { "*" },
        allowedHeaders = { "*" },
        maxAge = 900
)
@RestController
@RequestMapping("/sse-ping-server")
public class SeePingController {

    protected Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

    protected AtomicInteger pingCounter = new AtomicInteger(0);;
    protected AtomicInteger pingDelay = new AtomicInteger(2);

    protected AtomicInteger messageCounter = new AtomicInteger(0);;
    protected AtomicInteger messageDelay = new AtomicInteger(7);

    public SeePingController () {
        new Thread(() -> {
            while (true) {
                String pingMessage = "Ping: " + LocalTime.now().toString();
                ServerSentEvent<String> ping = ServerSentEvent.<String>builder()
                        .id(String.valueOf(pingCounter.getAndIncrement()))
                        .event("ping")
                        .data(pingMessage)
                        .build();

                sink.tryEmitNext(ping);

                try {
                    Thread.sleep(pingDelay.get() * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();

        new Thread(() -> {
            while (true) {
                String messageText = "Message: " + LocalTime.now().toString();
                ServerSentEvent<String> message = ServerSentEvent.<String>builder()
                        .id(String.valueOf(messageCounter.getAndIncrement()))
                        .event("message")
                        .data(messageText)
                        .build();

                sink.tryEmitNext(message);

                try {
                    Thread.sleep(messageDelay.get() * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }

    @GetMapping("/ping")
    public String setPing(@RequestParam(required = true) Integer delay){
        pingDelay = new AtomicInteger(delay);
        return "Set ping delay to " + delay;
    }

    @GetMapping("/message")
    public String setMessages(@RequestParam(required = true) Integer delay){
        messageDelay = new AtomicInteger(delay);
        return "Set messages delay to " + delay;
    }

    @GetMapping("/stream-sse")
    @ResponseBody
    public Flux<ServerSentEvent<String>> streamSink(){
        return sink.asFlux();
    }


}
