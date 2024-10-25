/*
 * Copyright (C) 2024 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.dvcli.action;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Processes a batch of labeled items by applying an action to each item. The labels are used for reporting. Typically, the label is the ID of the item. After each action, the processor waits for a
 * delay, if specified. The processor reports the results of the actions to a report.
 *
 * @param <I> the type of the items
 * @param <R> the type of action results
 * @see Report for the interface that the report must implement
 */
@Builder
@Slf4j
public class BatchProcessor<I, R>  {
    /**
     * The labeled items to process. The String is the label, <code>I</code> is the item.
     */
    @NonNull
    private final Stream<Pair<String, I>> labeledItems;

    /**
     * The number of items to process. If the labeled items are a collection, this number is the size of the collection. Otherwise, it is null.
     */
    private final Long numberOfItems;

    /**
     * The action to apply to each item.
     */
    @NonNull
    private final ThrowingFunction<I, R, Exception> action;

    /**
     * The report to which the results of the actions are reported.
     */
    @Builder.Default
    private final Report<I, R> report = new ConsoleReport<>();

    /**
     * The delay in milliseconds between processing items. A delay of 0 or less means no delay.
     */
    @Builder.Default
    private final long delay = 1000;

    public static class BatchProcessorBuilder<I, R> {
        public BatchProcessorBuilder<I, R> labeledItems(Collection<Pair<String, I>> items) {
            this.labeledItems = items.stream();
            this.numberOfItems = (long) items.size();
            return this;
        }

        public BatchProcessorBuilder<I, R> labeledItems(Stream<Pair<String, I>> items) {
            this.labeledItems = items;
            return this;
        }
    }

    public void process() {
        log.info("Starting batch processing of " + (numberOfItems == null ? "?" : numberOfItems + " items"));
        AtomicInteger i = new AtomicInteger(0);
        try {
            labeledItems.forEach(labeledItem -> {
                int index = i.incrementAndGet();
                delayIfNeeded(index);
                log.info("Processing item {} of {}: {}", index, numberOfItems == null ? "?" : numberOfItems, labeledItem.getFirst());
                callAction(labeledItem.getFirst(), labeledItem.getSecond());
            });
        } finally {
            labeledItems.close();
        }
        log.info("Finished batch processing of " + (numberOfItems == null ? "?" : numberOfItems + " items"));
    }

    private void callAction(String label, I item) {
        try {
            R r = action.apply(item);
            report.reportSuccess(label, item, r);
        }
        catch (Exception e) {
            report.reportFailure(label, item, e);
        }
    }

    private void delayIfNeeded(int i) {
        if (delay > 0 && i > 1) {
            log.debug("Sleeping for {} ms", delay);
            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

}
