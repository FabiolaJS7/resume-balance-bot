package com.bootcamp.resume.balance.bot.connector;

import com.bootcamp.commons.bean.products.ProductTypeResponse;
import com.bootcamp.resume.balance.bot.util.JsonTransferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductTypeConnector {

    WebClient webClient;
    AuthConnector authConnector;

    public ProductTypeConnector(@Qualifier("webClientService") WebClient webClient, AuthConnector authConnector) {
        this.webClient = webClient;
        this.authConnector = authConnector;
    }

    // Endpoint de product API para obtener detalles de los tipos de productos en el banco
    public Mono<ProductTypeResponse> getProductTypeByCode(String productTypeCode) {
        log.info("API getProductByCustomerId RQ: {}", productTypeCode);
        return authConnector.getAuthToken()
                .flatMap(token -> webClient.get()
                        .uri("/api/products/types/" + productTypeCode)
                        .header("Authorization",  token)
                        .retrieve()
                        .bodyToMono(ProductTypeResponse.class)
                        .doOnNext(productTypeResponse -> log.info("API getProductTypeByCode RQ: {}",
                                JsonTransferUtil.objectToJson(productTypeResponse)))
                        .doOnError(throwable -> log.error("API error getProductTypeByCode: {}",
                                throwable.getMessage()))
                );
    }

}
