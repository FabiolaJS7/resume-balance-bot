package com.bootcamp.resume.balance.bot.service;

import com.bootcamp.commons.bean.finance.DebtRequest;
import com.bootcamp.commons.bean.products.ProductResponse;
import com.bootcamp.commons.bean.products.ProductTypeResponse;
import com.bootcamp.resume.balance.bot.connector.FinanceConnector;
import com.bootcamp.resume.balance.bot.connector.ProductConnector;
import com.bootcamp.resume.balance.bot.connector.ProductTypeConnector;
import com.bootcamp.resume.balance.bot.constants.PaymentStatusConstants;
import com.bootcamp.resume.balance.bot.constants.ProductTypeConstants;
import com.bootcamp.resume.balance.bot.util.JsonTransferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@Slf4j
public class DebtServiceImpl implements DebtService {

    private static final int NEXT_MONTH = 1;
    private static final int INVOICE_DAY = 20;

    @Autowired
    ProductConnector productConnector;
    @Autowired
    FinanceConnector financeConnector;
    @Autowired
    ProductTypeConnector productTypeConnector;

    @Override
    public void saveDebtToCreditProducts() {

        productConnector.getProducts()
                .doOnNext(productResponse -> log.info("1. Getting active products ."))
                .filter(prs -> ProductTypeConstants.ACTIVE_PRODUCTS.contains(prs.getProductType()))
                .flatMap(productResponse -> {
                    log.info("2. Creating debt request of product {}.", productResponse.getId());
                    return productConnector.findBalanceByProductId(productResponse.getId())
                            .flatMap(balanceBeanResponse -> {
                                DebtRequest drb = new DebtRequest();
                                drb.setProductId(productResponse.getId());
                                drb.setProductType(productResponse.getProductType());
                                drb.setCustomerId(productResponse.getCustomer().getCustomerId());
                                drb.setCreditLimitUsed(balanceBeanResponse.getCreditLimitUsed());
                                // 20 del mes siguiente
                                drb.setPaymentDate(LocalDate.now().plusMonths(NEXT_MONTH).withDayOfMonth(INVOICE_DAY));
                                drb.setPaymentStatus(PaymentStatusConstants.PENDING_PAY);
                                return productTypeConnector.getProductTypeByCode(productResponse.getProductType())
                                        .flatMap(productTypeResponse -> {
                                            log.info("Maintance amount: {} and credit limit used: {}", productTypeResponse.getMaintenanceCommission(), drb.getCreditLimitUsed());
                                            drb.setMaintanceAmount(productTypeResponse.getMaintenanceCommission());
                                            drb.setTotalAmountToPay(productTypeResponse.getMaintenanceCommission() + drb.getCreditLimitUsed());
                                            log.info("Setted total amount to pay: {}", drb.getTotalAmountToPay());
                                            return Mono.just(drb);
                                        });
                            });
                }).flatMap(debtRequest -> financeConnector.createDebt(Mono.just(debtRequest)))
                .doOnNext(debtResponse -> log.info("4. Saving debt saved successfully: {}",
                        JsonTransferUtil.objectToJson(debtResponse)))
                .doOnError(throwable -> log.error("Saving debt failed {}", throwable.getMessage()))
                .subscribe();
    }

    private Mono<Double> getMaintanceAmount(ProductResponse productResponse) {
        return productTypeConnector.getProductTypeByCode(productResponse.getProductType())
                .map(ProductTypeResponse::getMaintenanceCommission)
                .defaultIfEmpty(0.00);
    }
}
