package com.bootcamp.resume.balance.bot.connector;

import com.bootcamp.commons.bean.finance.ResumeRequest;
import com.bootcamp.commons.bean.finance.ResumeResponse;
import com.bootcamp.resume.balance.bot.util.JsonTransferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FinanceConnector {

    WebClient webClient;

    public FinanceConnector(@Qualifier("webClientFinanceService") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ResumeResponse> createResume(Mono<ResumeRequest> resumeRequest) {
        log.info("API Create Resume RQ: {}", JsonTransferUtil.objectToJson(resumeRequest));
        return resumeRequest
                .doOnNext(rq -> log.info("API Create resume RQ: {}", JsonTransferUtil.objectToJson(rq)))
                .flatMap(rq -> webClient.post()
                        .uri("/api/finance/resumes")
                        .bodyValue(rq)
                        .retrieve()
                        .bodyToMono(ResumeResponse.class)
                        .doOnNext(resumeResponse -> log.info("API Create Resume RS: {}",
                                JsonTransferUtil.objectToJson(resumeResponse)))
                        .doOnError(throwable -> log.error("API error create resume {}", throwable.getMessage()))
                );
    }
}
