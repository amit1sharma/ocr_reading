package com.amt.ocr.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class InitOpenCV {
    static {
//        System.load("/usr/local/share/java/opencv4/libopencv_java430.so");
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
}
