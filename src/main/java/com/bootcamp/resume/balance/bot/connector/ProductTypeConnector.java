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

    public ProductTypeConnector(@Qualifier("webClientProductService") WebClient webClient) {
        this.webClient = webClient;
    }

    // Endpoint de product API para obtener detalles de los tipos de productos en el banco
    public Mono<ProductTypeResponse> getProductTypeByCode(String productTypeCode) {
        log.info("API getProductByCustomerId RQ: {}", productTypeCode);
        return webClient.get()
                .uri("/api/products/types/" + productTypeCode)
                .retrieve()
                .bodyToMono(ProductTypeResponse.class)
                .doOnNext(productTypeResponse -> log.info("API getProductTypeByCode RQ: {}",
                        JsonTransferUtil.objectToJson(productTypeResponse)))
                .doOnError(throwable -> log.error("API error getProductTypeByCode: {}",
                        throwable.getMessage()));
    }

}
