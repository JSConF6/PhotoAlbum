package com.squarecross.photoalbum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PhotoalbumApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoalbumApplication.class, args);
	}

}
