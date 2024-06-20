package nl.knaw.dans.dvcli.action;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Action implements Runnable {
    private Runnable action;
    private Report report;

    @Override
    public void run() {
        try {
            action.run();
        }
        catch (Exception e) {
            report.report(e.getMessage());
        }
    }
}
