package com.bootcamp.resume.balance.bot.schedule;

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

    //@Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "0 59 23 * * *") //correo a las 23:59 todos los días para guardar el saldo del día
    void process() {
        System.out.println("Processing tasks");
        resumeService.saveResumes();

    }
}
