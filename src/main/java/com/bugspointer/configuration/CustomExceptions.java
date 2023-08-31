package com.bugspointer.configuration;

public class CustomExceptions extends Exception{

    // Utilisation : throw new CustomExceptions.GetLocalDateException("Erreur liée à la date");

    public static class GetLocalDateException extends Exception {
        public GetLocalDateException(String message) {
            super(message);
        }
    }

    public static class GetDeleteMandateException extends Exception {
        public GetDeleteMandateException(String message) {
            super(message);
        }
    }

    public static class OtherCustomException extends RuntimeException {
        public OtherCustomException(String message) {
            super(message);
        }
    }
}
