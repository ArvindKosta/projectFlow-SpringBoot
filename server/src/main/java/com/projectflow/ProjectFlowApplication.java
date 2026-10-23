package com.projectflow;

import com.projectflow.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(AppProperties.class)
public class ProjectFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectFlowApplication.class, args);
    }
}
