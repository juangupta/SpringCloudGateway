package gateway.filter;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

	 private RestTemplate restTemplate = new RestTemplate();
	
	private static final String RESOURCE = "http://www.mocky.io/v2/5d9e0ef43200006100329bf5?mocky-delay=1000ms";
	private static final String DELAY_SERVICE_URL = "http://www.mocky.io";
	//
	
	private final WebClient webClient = WebClient.builder()
	        .baseUrl(DELAY_SERVICE_URL)
	        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	        .build();
	
	    public AuthorizationFilter() {
	    	super(Config.class);
	    }
	
	@Override
	  public GatewayFilter apply(Config config) {
	      return (exchange, chain) -> {
	    	  ServerHttpRequest request = exchange.getRequest();
	    	  
	    	  if (request.getHeaders().containsKey("X-Forwarded-For")) {	    		  
                  System.out.println("X-Forwarded-For: " + request.getHeaders().get("X-Forwarded-For").get(0));
              };
	    	  
	    	  if (!request.getHeaders().containsKey("Authorization")) {
                  return this.onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
              };
  
              String authorizationHeader = request.getHeaders().get("Authorization").get(0);
  
              if (!this.isAuthorizationValid(authorizationHeader)) {
                  return this.onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
              }
              
//              ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().
//                      header("secret", RandomStringUtils.random(10)).
//                      build();
	    	  
              return chain.filter(exchange.mutate().request(request).build());
	          
	      };
	  }
	
	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus)  {
		System.out.println(err);
		ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
	
	private boolean isAuthorizationValid(String authorizationHeader) {
        
//         String reponse = webClient.post()
//        .uri("/v2/5d9df5bb3200008d07329b21?mocky-delay=3000ms")
//        .retrieve()
//        .bodyToMono(String.class)
//        .toString();
        
		HttpEntity<String> request = new HttpEntity<>(new String("bar"));		
		String reponse = restTemplate.postForObject(RESOURCE, request, String.class);
//        
        System.out.println("Authorization header: " + authorizationHeader + " response: " + reponse);
        
        boolean isValid = true;
		
		if (reponse.equalsIgnoreCase("OK")) {
        	isValid = true;
        }else {
        	isValid = false;
        }	
        
        return isValid;
    }
	
	public static class Config {
        private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
    }
	
}
