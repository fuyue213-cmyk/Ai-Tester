package com.fuyue.domain.task.ability;

import java.util.Optional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public interface ITaskStreamManager {

    <F> Sinks.Many<F> registerStream(String taskId);

    <F> Optional<Flux<F>> getStream(String taskId);

    boolean emit(String taskId, Object data);

    boolean complete(String taskId);

    void cleanup(String taskId);

    boolean hasStream(String taskId);
}
