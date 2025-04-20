package com.bootcamp.resume.balance.bot.service;

import com.bootcamp.commons.bean.finance.ResumeRequest;
import com.bootcamp.commons.bean.products.ProductResponse;
import com.bootcamp.resume.balance.bot.connector.FinanceConnector;
import com.bootcamp.resume.balance.bot.connector.ProductConnector;
import com.bootcamp.resume.balance.bot.constants.ProductTypeConstants;
import com.bootcamp.resume.balance.bot.util.JsonTransferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    ProductConnector productConnector;
    @Autowired
    FinanceConnector financeConnector;

    @Override
    public void saveResumes() {

        productConnector.getProducts()
                .doOnNext(productResponse -> log.info("1. Getting products SA."))
                .flatMap(productResponse -> {
                    log.info("2. Creating resume request of product {}.", productResponse.getId());
                    return productConnector.findBalanceByProductId(productResponse.getId())
                            .doOnSuccess(balanceBeanResponse -> log.info("3. Balance of product {}, {}",
                                    productResponse.getId(), JsonTransferUtil.objectToJson(balanceBeanResponse)))
                            .flatMap(balanceBeanResponse -> {
                                ResumeRequest resumeRequest = new ResumeRequest();
                                resumeRequest.setProductId(productResponse.getId());
                                resumeRequest.setProductType(ProductTypeConstants.COMPLETE_PRODUCTS_NAME.get(productResponse.getProductType()));
                                resumeRequest.setCustomerId(productResponse.getCustomer().getCustomerId());
                                resumeRequest.setCreditEnabledToUse(balanceBeanResponse.getCreditEnabledToUse());
                                resumeRequest.setCreditLimitTotal(balanceBeanResponse.getCreditLimit());
                                resumeRequest.setCreditLimitUsed(balanceBeanResponse.getCreditLimitUsed());
                                resumeRequest.setTotalAmountInAccount(balanceBeanResponse.getTotalAmountInAccount());
                                resumeRequest.setInformDate(LocalDate.now());
                                return Mono.just(resumeRequest);
                            });
                })
                .flatMap(resumeRequest -> financeConnector.createResume(Mono.just(resumeRequest)))
                .doOnNext(resumeResponse -> log.info("4. Saving resume saved successfully: {}",
                        JsonTransferUtil.objectToJson(resumeResponse)))
                .doOnError(throwable -> log.error("Saving resume failed {}", throwable.getMessage()))
                .subscribe();
    }
}
