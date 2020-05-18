package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
public class SimulatorServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(SimulatorServiceApplication.class, args);
    }

    //https://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html#scheduling-task-scheduler-implementations
    //ThreadPoolTaskScheduler actually implements Spring’s TaskExecutor
    @Bean
    public AsyncTaskExecutor taskExecutor(){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        return scheduler;
    }
}

