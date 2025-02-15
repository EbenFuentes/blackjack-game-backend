package com.ebenfuentes.blackjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.ebenfuentes")
public class BlackjackGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlackjackGameApplication.class, args);
	}

}
