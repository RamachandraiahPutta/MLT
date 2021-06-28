package com.example.mlt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories
public class MltApplication {

	public static void main(String[] args) {
		SpringApplication.run(MltApplication.class, args);
	}

}
