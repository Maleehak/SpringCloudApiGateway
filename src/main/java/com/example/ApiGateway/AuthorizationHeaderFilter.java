package com.example.ApiGateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

  public AuthorizationHeaderFilter() {
    super(Config.class);
  }

  public static class Config {

  }

  @Override
  public GatewayFilter apply(Config config) {

    return (exchange, chain) -> {
      ServerHttpRequest httpRequest = exchange.getRequest();
      if (!httpRequest.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        return onError(exchange, "No authorization headers", HttpStatus.UNAUTHORIZED);
      }

      String authorizationHeader = httpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
      String jwt = authorizationHeader.replace("Bearer", "");

      if(!isJwtValid(jwt)){
        return onError(exchange, "Invalid jwt token", HttpStatus.UNAUTHORIZED);
      }

      return chain.filter(exchange);
    };
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    return response.setComplete();
  }

  private boolean isJwtValid(String jwt){
    boolean isValid =true;
    String subject = null;

    String tokenSecret = "eSepR60L9m3dClu7G6t6nXWzBRuWXqSEyAzNL7jDIctGR2Vk83wAQltam3F9Wh9n";

    Instant now = Instant.now();

    byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
    SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

    JwtParser jwtParser = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build();

    try{
      Jwt<Header, Claims> parsedToken = jwtParser.parse(jwt);
      subject = parsedToken.getBody().getSubject();
    }catch (Exception e){
      isValid = false;
    }
    if(subject == null || subject.isEmpty()) isValid = false;

    return isValid;
  }

}
