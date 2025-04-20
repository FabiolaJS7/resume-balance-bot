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

    public ProductConnector(@Qualifier("webClientProductService") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ProductResponse> getProducts() {
        log.info("API get products");
        return webClient.get()
                .uri("/api/products")
                .retrieve()
                .bodyToFlux(ProductResponse.class)
                .doOnNext(productResponse -> log.info("API getting product: {}",
                        JsonTransferUtil.objectToJson(productResponse)))
                .doOnError(throwable -> log.error("API getting products failed {}", throwable.getMessage()));
    }

    public Mono<BalanceBeanResponse> findBalanceByProductId(String productId) {
        log.info("API findBalanceByProductId RQ: {}", productId);
        return webClient.get()
                .uri("/api/products/" + productId + "/balance")
                .retrieve()
                .bodyToMono(BalanceBeanResponse.class)
                .doOnNext(balanceBeanResponse -> log.info("API findBalanceByProductId RS: {}",
                        JsonTransferUtil.objectToJson(balanceBeanResponse)))
                .doOnError(error -> log.error("Error API while getting balance by product: {}", error.getMessage()));
    }
}
