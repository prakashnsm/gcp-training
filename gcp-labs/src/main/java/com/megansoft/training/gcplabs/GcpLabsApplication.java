package com.megansoft.training.gcplabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.google.common.base.Predicate;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableAutoConfiguration(exclude=DataSourceAutoConfiguration.class)
public class GcpLabsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GcpLabsApplication.class, args);
	}

	@Bean
	public Docket productApi() {
		String bsPackage = "com.megansoft.training.gcplabs";
		Predicate<RequestHandler> basePackage = RequestHandlerSelectors
				.basePackage(bsPackage);
		DocumentationType swagger2 = DocumentationType.SWAGGER_2;
		return new Docket(swagger2).select()
				.apis(basePackage)
				.build();
	}
}

