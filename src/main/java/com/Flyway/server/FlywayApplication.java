package com.Flyway.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class FlywayApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlywayApplication.class, args);
	}

}
