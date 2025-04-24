package com.bootcamp.resume.balance.bot.connector;

import com.bootcamp.commons.bean.finance.DebtRequest;
import com.bootcamp.commons.bean.finance.DebtResponse;
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
    AuthConnector authConnector;

    public FinanceConnector(@Qualifier("webClientService") WebClient webClient, AuthConnector authConnector) {
        this.webClient = webClient;
        this.authConnector = authConnector;
    }

    public Mono<ResumeResponse> createResume(Mono<ResumeRequest> resumeRequest) {
        return authConnector.getAuthToken()
                .flatMap(token -> resumeRequest
                        .doOnNext(rq -> log.info("API Create resume RQ: {}", JsonTransferUtil.objectToJson(rq)))
                        .flatMap(rq -> webClient.post()
                                .uri("/api/finance/resumes")
                                .header("Authorization", token)
                                .bodyValue(rq)
                                .retrieve()
                                .bodyToMono(ResumeResponse.class)
                                .doOnNext(resumeResponse -> log.info("API Create Resume RS: {}",
                                        JsonTransferUtil.objectToJson(resumeResponse)))
                                .doOnError(throwable -> log.error("API error create resume {}", throwable.getMessage()))
                        )
                );
    }

    public Mono<DebtResponse> createDebt(Mono<DebtRequest> debtRequest) {
        return authConnector.getAuthToken()
                .flatMap(token -> debtRequest
                        .doOnNext(rq -> log.info("API create debt RQ {}", JsonTransferUtil.objectToJson(rq)))
                        .flatMap(rq -> webClient.post()
                                .uri("/api/finance/debts")
                                .header("Authorization", token)
                                .bodyValue(rq)
                                .retrieve()
                                .bodyToMono(DebtResponse.class)
                                .doOnNext(rs -> log.info("API create debt RS {}", JsonTransferUtil.objectToJson(rs)))
                                .doOnError(throwable -> log.error("API error create debt {}", throwable.getMessage()))
                        )
                );
    }
}
