package com.bootcamp.resume.balance.bot.schedule;

import com.bootcamp.resume.balance.bot.service.DebtService;
import com.bootcamp.resume.balance.bot.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class CustomTaskScheduler {

    @Autowired
    ResumeService resumeService;
    @Autowired
    DebtService debtService;

    //@Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "0 59 23 * * *") // correr a las 23:59 todos los días para guardar el saldo del día
    void process() {
        log.info("Processing tasks");
        resumeService.saveResumes();
    }

    //@Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "0 0 0 15 * *") // Corre a las 00:00 del día 15 de cada mes para guardar las deudas de los productos de crédito
    void generateDebt() {
        log.info("Processing tasks to generate debt");
        debtService.saveDebtToCreditProducts();
    }
}
