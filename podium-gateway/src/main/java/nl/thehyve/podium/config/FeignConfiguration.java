package nl.thehyve.podium.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "nl.thehyve.podium.client")
public class FeignConfiguration {

}
