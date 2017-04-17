import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actors.MetricSuscriber;
import akka.beans.Task;
import akka.config.SpringExtension;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.metrics.Metrics;
import kamon.Kamon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

@Configuration
@EnableAutoConfiguration
@ComponentScan("akka.config")
public class MainApp {

    private static SpringExtension springExtension;
    private static ActorRef supervisor;
    private static ApplicationContext applicationContext;
    private static ActorSystem system;
    private static LoggingAdapter log;
    private static ActorRef metricSuscriptor;

    public static void main(String[] args) throws Exception {
        Kamon.start();
        initValues(args);
        createTasks();
        terminateApplication(applicationContext, system, log);
    }

    private static void initValues(String[] args) {
        applicationContext = SpringApplication.run(MainApp.class, args);
        system = applicationContext.getBean(ActorSystem.class);
        log = Logging.getLogger(system, "Application");
        log.info("Starting up");
        springExtension = applicationContext.getBean(SpringExtension.class);
        supervisor = system.actorOf(springExtension.props("supervisor").withMailbox("akka.priority-mailbox"));
        metricSuscriptor = system.actorOf(Props.create(MetricSuscriber.class), "metric-subscriber");
    }

    private static void createTasks() throws InterruptedException {
        Metrics.startTraceContext();

        for(int j = 1; j <= 10; j++){
            System.out.println("iteration: " + j);
            for (int i = 1; i < 100; i++) {
                Task task = new Task("payload " + i, new Random().nextInt(99));
                supervisor.tell(task, null);
            }
            Thread.sleep(10000);
        }
        Metrics.finishTraceContext();
    }

    private static void terminateApplication(ApplicationContext context, ActorSystem system, LoggingAdapter log)
            throws InterruptedException {
        supervisor.tell(PoisonPill.getInstance(), null);
        while (!supervisor.isTerminated()) {Thread.sleep(100);}

        log.info("Created {} tasks", context.getBean(JdbcTemplate.class)
                .queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));
        log.info("Shutting down");

        shutDownSystem(system);
    }

    private static void shutDownSystem(ActorSystem system) {
        Kamon.shutdown();
        system.shutdown();
        system.awaitTermination();
    }
}
