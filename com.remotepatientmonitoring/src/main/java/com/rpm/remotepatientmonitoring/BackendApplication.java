package com.rpm.remotepatientmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.scheduling.annotation.EnableAsync; // CHANGED

@SpringBootApplication
@EnableAsync // CHANGED
@ComponentScan(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
