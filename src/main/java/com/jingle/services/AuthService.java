package com.jingle.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

import com.jingle.exceptions.*;
import com.jingle.models.User;

@Service
public class AuthService {
	
	private Map<String, Long> authKeys = new HashMap<String, Long>();
	private Map<String, Date> authKeyExpires = new HashMap<String, Date>();
	
	// Default authentication key timeout of 20 mins
	private final long DEFAULT_AUTH_TIMEOUT = 1200000;
	
	/**
	 * Gets a hash of the given password
	 */
	public byte[] getPasswordHash(String password) throws 	NoSuchAlgorithmException,
	  														InvalidKeySpecException {
		
		byte[] salt = new byte[20];
		for(int i = 0; i < salt.length; i++) {
			salt[i] = (byte)password.charAt(password.length()-1-(i%password.length()));
		}		
		
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		
		return factory.generateSecret(spec).getEncoded();
	}
	
	/**
	 * Creates a new authentication key for a given userid
	 */
	public String newAuthKey(Long userId) {
		return newAuthKey(userId, DEFAULT_AUTH_TIMEOUT);
	}
	
	/**
	 * Creates a new authentication key for a given userid, with an expiry time of
	 * authTimeout milliseconds
	 */
	public String newAuthKey(long userId, long authTimeout) {
		String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";

		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < 30; i++) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		
		String authKey = builder.toString();
		
		authKeys.put(authKey, userId);
		authKeyExpires.put(authKey, new Date(new Date().getTime() + authTimeout));
		
		return authKey;
	}
	
	/**
	 * Check if the given authentication key is valid for the given userid
	 */
	public boolean isValidAuthKey(String authKey, User user) throws ExpiredAuthKeyException, InvalidAuthKeyException {
		
		if(user.getId() == null) {
			throw new IllegalArgumentException("User id cannot be null");
		}
		
		Long userIdAuth = authKeys.get(authKey);
		Date expiresAt = authKeyExpires.get(authKey);
		
		if (userIdAuth == null || !userIdAuth.equals(user.getId()) || expiresAt == null) {
			throw new InvalidAuthKeyException();
		}
		
		if (expiresAt.before(new Date())) {
			throw new ExpiredAuthKeyException();
		}
		
		return true;
	}
}
