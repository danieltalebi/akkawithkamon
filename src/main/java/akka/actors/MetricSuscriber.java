package akka.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kamon.Kamon;
import kamon.metric.Entity;
import kamon.metric.EntitySnapshot;
import kamon.metric.instrument.Counter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import kamon.metric.SubscriptionsDispatcher.TickMetricSnapshot;
import scala.collection.JavaConversions;

import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Scope("prototype")
public class MetricSuscriber extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public int taskCounter = 0;
    public long traceCounterTime;

    public MetricSuscriber() {
        Kamon.metrics().subscribe("counter", "task-counter", getSelf());
        Kamon.metrics().subscribe("trace", "trace-context", getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof TickMetricSnapshot) {
            TickMetricSnapshot snapshot = (TickMetricSnapshot) message;
            Map<Entity, EntitySnapshot> metricsMap = getMetricsMap(snapshot);

            if (metricsMap.values().isEmpty()) {
                return;
            }
            showCounterStatus(metricsMap);
        }
    }

    private Map<Entity, EntitySnapshot> getMetricsMap(TickMetricSnapshot snapshot) {
        return JavaConversions.mapAsJavaMap(snapshot.metrics());
    }

    private void showCounterStatus(Map<Entity, EntitySnapshot> metricsMap) {
        try {
            metricsMap.forEach((e, s) -> {
                final Counter.Snapshot counterSnapshot = s.counter("counter").get();
                taskCounter += counterSnapshot.count();
            });
        } catch (NoSuchElementException e) {
            System.out.println("No Counters found");
        }
        System.out.println("taskCounter AA: " + this.taskCounter);
    }

}

