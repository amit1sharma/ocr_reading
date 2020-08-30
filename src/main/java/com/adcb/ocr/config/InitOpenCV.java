package com.adcb.ocr.config;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitOpenCV {
	private static final Logger APPLOGGER = LoggerFactory.getLogger(InitOpenCV.class);

    static {
    	APPLOGGER.info("Loading Native library.....");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    //	System.load("/home/appuser/opencv-4.3.0/build/lib/libopencv_java430.so");
    }
}
