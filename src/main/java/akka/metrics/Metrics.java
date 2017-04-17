package akka.metrics;

import kamon.Kamon;
import kamon.metric.instrument.Counter;
import kamon.trace.TraceContext;

public class Metrics {

    private static Counter taskCounter = Kamon.metrics().counter("task-counter");
    private static Counter taskFailed = Kamon.metrics().counter("failed-counter");
    private static TraceContext traceContext;

    public static void incrementTaskCounter() {
        taskCounter.increment();
    }

    public static void startTraceContext() {
        traceContext = Kamon.tracer().newContext("trace-context");
    }

    public static void finishTraceContext() {
        traceContext.finish();
    }


}
