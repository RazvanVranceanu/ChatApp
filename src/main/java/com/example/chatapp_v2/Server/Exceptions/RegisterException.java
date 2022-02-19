package com.example.chatapp_v2.Server.Exceptions;

public class RegisterException extends Exception{
    public RegisterException(String message) {super(message);}

    @Override
    public String getMessage(){
        return "Register error: " + super.getMessage();
    }
}
