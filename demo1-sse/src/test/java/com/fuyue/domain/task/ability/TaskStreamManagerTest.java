package com.fuyue.domain.task.ability;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskStreamManagerTest {

    private final TaskStreamManager taskStreamManager = new TaskStreamManager();

    @Test
    void shouldRegisterAndReuseSameStream() {
        assertSame(taskStreamManager.registerStream("task-1"), taskStreamManager.registerStream("task-1"));
        assertTrue(taskStreamManager.hasStream("task-1"));
    }

    @Test
    void shouldEmitAndCompleteStream() {
        taskStreamManager.registerStream("task-2");
        Flux<String> stream = taskStreamManager.<String>getStream("task-2").orElseThrow();

        StepVerifier.create(stream)
                .then(() -> assertTrue(taskStreamManager.emit("task-2", "hello")))
                .expectNext("hello")
                .then(() -> assertTrue(taskStreamManager.complete("task-2")))
                .verifyComplete();

        assertFalse(taskStreamManager.hasStream("task-2"));
    }

    @Test
    void shouldCleanupStream() {
        taskStreamManager.registerStream("task-3");
        assertTrue(taskStreamManager.hasStream("task-3"));

        taskStreamManager.cleanup("task-3");

        assertFalse(taskStreamManager.hasStream("task-3"));
        assertTrue(taskStreamManager.getStream("task-3").isEmpty());
    }
}
