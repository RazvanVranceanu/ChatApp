package com.example.chatapp_v2.Server.Exceptions;

public class LoginException extends Exception{
    public LoginException(String message) {super(message);}

    @Override
    public String getMessage(){
        return "Login error: " + super.getMessage();
    }
}
