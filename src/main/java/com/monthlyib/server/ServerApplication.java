package com.monthlyib.server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;


@SpringBootApplication
@EnableJpaAuditing
@PropertySource("classpath:/env.yml")
@EnableScheduling
public class ServerApplication {

	@PostConstruct
	public void started() {
//		TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
