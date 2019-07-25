package com.jingle.exceptions;

public class InvalidAuthKeyException extends Exception {

	private static final long serialVersionUID = -6875269451301987197L;

	public String getMessage() {
		return "Invalid auth key";
	}
}
