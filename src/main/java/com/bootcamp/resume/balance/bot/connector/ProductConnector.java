package com.bootcamp.resume.balance.bot.connector;

import com.bootcamp.commons.bean.products.BalanceBeanResponse;
import com.bootcamp.commons.bean.products.ProductResponse;
import com.bootcamp.resume.balance.bot.util.JsonTransferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductConnector {

    WebClient webClient;
    AuthConnector authConnector;

    public ProductConnector(@Qualifier("webClientService") WebClient webClient, AuthConnector authConnector) {
        this.webClient = webClient;
        this.authConnector = authConnector;
    }

    public Flux<ProductResponse> getProducts() {
        log.info("API get products");
        return authConnector.getAuthToken()
                .flatMapMany(token -> webClient.get()
                        .uri("/api/products")
                        .header("Authorization",  token)
                        .retrieve()
                        .bodyToFlux(ProductResponse.class)
                        .doOnNext(productResponse -> log.info("API getting product: {}",
                                JsonTransferUtil.objectToJson(productResponse)))
                        .doOnError(throwable -> log.error("API getting products failed {}", throwable.getMessage()))
                );
    }

    public Mono<BalanceBeanResponse> findBalanceByProductId(String productId) {
        log.info("API findBalanceByProductId RQ: {}", productId);
        return authConnector.getAuthToken()
                .flatMap(token -> webClient.get()
                        .uri("/api/products/" + productId + "/balance")
                        .header("Authorization",  token)
                        .retrieve()
                        .bodyToMono(BalanceBeanResponse.class)
                        .doOnNext(balanceBeanResponse -> log.info("API findBalanceByProductId RS: {}",
                                JsonTransferUtil.objectToJson(balanceBeanResponse)))
                        .doOnError(error -> log.error("Error API while getting balance by product: {}", error.getMessage()))
                );
    }
}
