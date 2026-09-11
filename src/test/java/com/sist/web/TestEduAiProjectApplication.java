package com.sist.web;

import org.springframework.boot.SpringApplication;

public class TestEduAiProjectApplication {

	public static void main(String[] args) {
		SpringApplication.from(EduAiProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
