package akka.config;

import akka.actor.ActorSystem;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@Lazy
@ComponentScan(basePackages = {"akka.actors", "akka.services", "akka.extension"})
public class ApplicationConfiguration {

    public static final String SQL_TASK_QUERY = "CREATE TABLE tasks (id INT(11) AUTO_INCREMENT, " +
            "payload VARCHAR(255), updated DATETIME)";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem
                .create("AkkaTaskProcessing", akkaConfiguration());
        springExtension.initialize(applicationContext);
        return system;
    }

    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() throws Exception {
        disableUnnecessaryLogging();
        final ComboPooledDataSource source = createDataBase();
        JdbcTemplate template = new JdbcTemplate(source);
        template.update(SQL_TASK_QUERY);
        return template;
    }

    private ComboPooledDataSource createDataBase() throws PropertyVetoException {
        final ComboPooledDataSource source = new ComboPooledDataSource();
        source.setMaxPoolSize(100);
        source.setDriverClass("org.h2.Driver");
        source.setJdbcUrl("jdbc:h2:mem:taskdb");
        source.setUser("sa");
        source.setPassword("");
        return source;
    }

    private void disableUnnecessaryLogging() {
        final Properties properties = new Properties(System.getProperties());
        properties.put("com.mchange.v2.log.MLog",
                "com.mchange.v2.log.FallbackMLog");
        properties.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL",
                "OFF");
        System.setProperties(properties);
    }
}
