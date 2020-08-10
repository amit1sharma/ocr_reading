package com.adcb.ocr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatupConfigurator implements CommandLineRunner {
    
 
//    public static int counter;
    
    public static String saveStageImages;
    
    @Value("${saveStageImage}")
    private String saveStageImage;
    
 
    @Override
    public void run(String...args) throws Exception {
//        counter++;
//        System.out.println(counter);
        saveStageImages = this.saveStageImage;
    }
}