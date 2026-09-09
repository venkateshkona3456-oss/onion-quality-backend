package com.onionquality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
@SpringBootApplication
public class OnionQualityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnionQualityBackendApplication.class, args);
    }

  @Bean
public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(90000);
    factory.setReadTimeout(90000);
    return new RestTemplate(factory);
}
}