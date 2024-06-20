package nl.knaw.dans.dvcli.action;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@Builder
@Slf4j
public class BatchProcessor<I> {
    private Iterable<I> items;
    private Action action;

    private long delay;

    public void process() {
        int i = 0;
        for (var item : items) {
            delayIfNeeded(i);
            logStartAction(++i);
            action.apply(item);
        }
    }

    private void delayIfNeeded(int i) {
        if (delay > 0 && i > 0) {
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

    private void logStartAction(int i) {
        if (items instanceof List) {
            log.info("Processing item {} of {}", i, ((List<?>) items).size());
        }
        else {
            log.info("Processing item {}", i);
        }
    }
}
