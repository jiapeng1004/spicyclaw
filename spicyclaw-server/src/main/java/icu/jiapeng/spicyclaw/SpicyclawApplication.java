package icu.jiapeng.spicyclaw;

import io.agentscope.spring.boot.AgentscopeAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = AgentscopeAutoConfiguration.class)
public class SpicyclawApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpicyclawApplication.class, args);
    }

}
