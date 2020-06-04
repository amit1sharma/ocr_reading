package com.amt.ocr.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class InitOpenCV {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
}
