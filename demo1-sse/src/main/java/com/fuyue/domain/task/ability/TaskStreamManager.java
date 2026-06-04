package com.fuyue.domain.task.ability;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
public class TaskStreamManager implements ITaskStreamManager {

    private final ConcurrentHashMap<String, Sinks.Many<Object>> streams = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <F> Sinks.Many<F> registerStream(String taskId) {
        return (Sinks.Many<F>) streams.computeIfAbsent(taskId, key -> {
            Sinks.Many<Object> sink = Sinks.many().multicast().onBackpressureBuffer();
            log.debug("Registered stream for taskId: {}", taskId);
            return sink;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F> Optional<Flux<F>> getStream(String taskId) {
        Sinks.Many<F> sink = (Sinks.Many<F>) streams.get(taskId);
        return sink == null ? Optional.empty() : Optional.of(sink.asFlux());
    }

    @Override
    public boolean emit(String taskId, Object data) {
        Sinks.Many<Object> sink = streams.get(taskId);
        if (sink == null) {
            log.warn("Stream not found for taskId: {}, cannot emit data", taskId);
            return false;
        }

        Sinks.EmitResult result = sink.tryEmitNext(data);
        if (result.isFailure()) {
            log.warn("Failed to emit data for taskId: {}, result: {}", taskId, result);
            return false;
        }
        return true;
    }

    @Override
    public boolean complete(String taskId) {
        Sinks.Many<Object> sink = streams.get(taskId);
        if (sink == null) {
            log.warn("Stream not found for taskId: {}, cannot complete", taskId);
            return false;
        }

        Sinks.EmitResult result = sink.tryEmitComplete();
        if (result.isFailure()) {
            log.warn("Failed to complete stream for taskId: {}, result: {}", taskId, result);
            return false;
        }

        log.debug("Completed stream for taskId: {}", taskId);
        cleanup(taskId);
        return true;
    }

    @Override
    public void cleanup(String taskId) {
        Sinks.Many<Object> sink = streams.remove(taskId);
        if (sink != null) {
            log.debug("Cleaned up stream for taskId: {}", taskId);
        }
    }

    @Override
    public boolean hasStream(String taskId) {
        return streams.containsKey(taskId);
    }
}
