package com.example.ApiGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalFilterConfiguration {

  final Logger logger = LoggerFactory.getLogger(GlobalFilterConfiguration.class);

  @Bean
  @Order(1)
  public GlobalFilter secondGlobalFilter() {
    return (exchange, chain)-> {
      logger.info("Entered into second global pre filter");
      return chain.filter(exchange).then(Mono.fromRunnable(()->{
        logger.info("Entered into second global post filter");
      }));
    };
  }

  @Bean
  @Order(2)
  public GlobalFilter thirdGlobalFilter() {
    return (exchange, chain)-> {
      logger.info("Entered into third global pre filter");
      return chain.filter(exchange).then(Mono.fromRunnable(()->{
        logger.info("Entered into third global post filter");
      }));
    };
  }
}
