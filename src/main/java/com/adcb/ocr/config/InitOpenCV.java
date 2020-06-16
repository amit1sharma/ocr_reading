package com.adcb.ocr.config;

import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sourceforge.tess4j.Tesseract;

@Configuration
public class InitOpenCV {
	
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
