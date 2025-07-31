package com.example.demo.exception;

public class EmailAlreadyExistsException extends RuntimeException{
	
	public EmailAlreadyExistsException(String msg)
	{
		super(msg);
	}

}
