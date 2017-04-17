package akka.beans;

public class Task {

    private String payload;

    private Integer priority;

    public Task(final String payload, final Integer priority) {
        this.payload = payload;
        this.priority = priority;
    }

    public String getPayload() {
        return payload;
    }

    public Integer getPriority() {
        return priority;
    }
}
