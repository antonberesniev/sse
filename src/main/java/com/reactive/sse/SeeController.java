package com.reactive.sse;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalTime;

@CrossOrigin(value = { "*" },
        allowedHeaders = { "*" },
        maxAge = 900
)
@RestController
@RequestMapping("/sse-server")
public class SeeController {

    protected Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

    protected int counter = 0;

    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents(@RequestParam(required = false) Integer delay){
        return Flux.interval(Duration.ofSeconds(delay == null ? 1 : delay))
                .map(sequence -> {
                    update("SSE - " + LocalTime.now().toString());
                    return ServerSentEvent.<String>builder()
                            .id(String.valueOf(sequence))
                            .event("message")
                            .data("SSE - " + LocalTime.now().toString())
                            .build();
                });
    }

    @GetMapping("/stream-sink")
    @ResponseBody
    public Flux<ServerSentEvent<String>> streamSink(){
        return sink.asFlux();
    }

    public void update(String update) {
        ServerSentEvent<String> sse = ServerSentEvent.<String>builder()
                .id(String.valueOf(counter++))
                .event("message")
                .data(update)
                .build();
        sink.tryEmitNext(sse);
    }

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux(@RequestParam(required = false) Integer delay) {
        return Flux.interval(Duration.ofSeconds(delay == null ? 1 : delay))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }





}
