package com.suszkolabs.utils;

import java.util.Map;

public class ScrapperInitializationException extends RuntimeException {
    public ScrapperInitializationException(Map<String, String> formData){
        super("Invalid loggin attempt for data: " + formData + ".\nRember to setup your environment variables properly.");
    }
}
