package hu.levente.prog3.concurrent;

import hu.levente.prog3.model.Schema;
import hu.levente.prog3.parser.SchemaParseException;
import hu.levente.prog3.parser.SchemaParser;
import hu.levente.prog3.util.Result;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncSchemaProcessor implements AutoCloseable {
    private final SchemaParser parser;
    private final ScheduledExecutorService debounceScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private ScheduledFuture<?> pendingTask;

    /**
     * Létrehoz egy új példányt a megadott parserrel.
     * @param parser nem lehet {@code NULL}
     * */
    public AsyncSchemaProcessor(SchemaParser parser) {
        this.parser = Objects.requireNonNull(parser, "A SchemaParser nem lehet null");
    }

    /**
     * Szintaktikai és szemantikai elemzést végez a megadott forráskódra.
     * Amennyiben a késleltetési időn belül egy újabb hívás érkezik, a korábbi még el nem indított kérés törlődik.
     *
     * @param text a feldolgozandó DSL forráskód
     * @param delayMillis a várakozási idő ezredmádorpercben az elemzés tényleges indítása előtt
     * */
    public synchronized void scheduleParsing(String text, long delayMillis, Consumer<Schema> onSuccess, Consumer<SchemaParseException> onError) {
        if (pendingTask != null && !pendingTask.isDone()) pendingTask.cancel(false);

        pendingTask = debounceScheduler.schedule(() -> {
            executorService.submit(() -> {
                Result<Schema, SchemaParseException> result = parser.parse(text);
                SwingUtilities.invokeLater(() -> {
                    result.ifSuccess(onSuccess);
                    result.ifFailure(onError);
                });
            });
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Leállítja az időzítőt és a virtális szálakat is felszabadítja.
     * */
    @Override
    public void close() {
        debounceScheduler.shutdown();
        executorService.shutdown();
    }

    /**
     * Végrehajt egy tetszőleges háttérfeladatot egy dedikált virtuális szálon
     *
     * @param task a végrehajtandó művelet
     * */
    public void runIoTask(Runnable task, Runnable onFinish, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            try {
                task.run();
                if (onFinish != null) SwingUtilities.invokeLater(onFinish);
            } catch (Throwable t) {
                if (onError != null) SwingUtilities.invokeLater(() -> onError.accept(t));
            }
        });
    }
}
