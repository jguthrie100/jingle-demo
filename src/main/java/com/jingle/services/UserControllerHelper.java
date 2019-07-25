package com.jingle.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.jingle.exceptions.ExpiredAuthKeyException;
import com.jingle.exceptions.InvalidAuthKeyException;
import com.jingle.models.User;
import com.jingle.repositories.UserRepository;

/**
 * Service class for APIController.
 * Keeps bulk of the logic out of the controller
 * 
 */
@Service
public class UserControllerHelper {
	
	@Autowired
	private UserRepository userRepository;
	
	private AuthService authService;
	
	@Autowired
	UserControllerHelper(UserRepository userRepository) {
		this.userRepository = userRepository;
		this.authService = new AuthService();
	}
	
	/**
	 * Save user to database
	 */
	public ResponseEntity<User> saveUser(User user) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] passwordHash = authService.getPasswordHash(new String(user.getPassHash()));
		user.setPasswordHash(passwordHash);
		
		User savedUser = userRepository.save(user);
		
		return new ResponseEntity<User>(savedUser, HttpStatus.CREATED);
	}

	/**
	 * Login with username and password - returns Authentication Key
	 */
	public ResponseEntity<Map<String, Object>> loginUser(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, FailedLoginException {

		Map<String, Object> output = new HashMap<String, Object>();

		String authKey;
		
		try {
			
			authKey = getAuthKey(username, password);
		
		} catch(IllegalArgumentException ex) {
			// If login password is too short, don't reveal real reason - just return FailedLogin
			if(ex.getMessage().equals("Password must be a minimum of 8 characters long")) {
				throw new FailedLoginException();
			} else {
				throw ex;
			}
		}
		Long userId = userRepository.findByUsername(username).get().getId();
		
		output.put("id", userId);
		output.put("authKey", authKey);
		
		return new ResponseEntity<Map<String, Object>>(output, HttpStatus.OK);
	}
	
	/**
	 * Edit existing user - returns updated user 
	 */
	public ResponseEntity<User> editUser(Long userId, String authKey, String username, String firstName, String lastName, String emailAddress, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, ExpiredAuthKeyException, InvalidAuthKeyException {
		
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
		
		authService.isValidAuthKey(authKey, user);
		
		if(username != null) user.setUsername(username);
		
		if(firstName != null) user.setFirstName(firstName);
		
		if(lastName != null) user.setLastName(lastName);
		
		if(emailAddress != null) user.setEmailAddress(emailAddress);
		
		if(password != null) user.setPasswordHash(authService.getPasswordHash(password));
		
		User updatedUser = userRepository.save(user);
		
		return new ResponseEntity<User>(updatedUser, HttpStatus.OK);
	}
	
	/**
	 * Deletes a user from the database - returns whether successful or not
	 */
	public ResponseEntity<Map<String, Object>> deleteUser(Long userId, String authKey) throws ExpiredAuthKeyException, InvalidAuthKeyException {
		Map<String, Object> output = new HashMap<String, Object>();
		
		User userToDelete = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
		
		authService.isValidAuthKey(authKey, userToDelete);
		userRepository.deleteById(userId);
		
		output.put("id", userId);
		output.put("success", true);
		
		return new ResponseEntity<Map<String, Object>>(output, HttpStatus.OK);
	}
	
	/**
	 * Returns a User from the database
	 */
	public ResponseEntity<User> getUser(Long userId, String username) {
		
		User user;
		

		if(userId != null) {
			user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
		
		} else if(username != null) {
			user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User with username (" + username + ") doesn't exist"));
		
		} else {
			throw new IllegalArgumentException("Either a userid or username must be provided");
		}
		
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	/**
	 * Returns an authorization key if the username matches the password
	 */
	private String getAuthKey(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, FailedLoginException {
		
		User user = userRepository.findByUsername(username).orElseThrow(() -> new FailedLoginException());
		
		if(Arrays.equals(user.getPassHash(), authService.getPasswordHash(password))) {
			return authService.newAuthKey(user.getId());
		
		} else {
			throw new FailedLoginException();
		}
	}
	
	public ResponseEntity<Map<String, Object>> exceptionHandler(HttpServletRequest req, Exception ex) {
		Map<String, Object> output = new HashMap<String, Object>();
		HttpStatus errorCode;
		
		output.put("error", ex.getMessage());
		
		// Check if UNIQUE index triggered
		if(ex.getClass().equals(DataIntegrityViolationException.class)) {
			if(ex.getMessage().indexOf("USER(USERNAME)") > -1) {
				output.put("error", "Username already taken");
			} else if(ex.getMessage().indexOf("USER(EMAIL_ADDRESS)") > -1) {
				output.put("error", "Email address already taken");
			} else {
				output.put("error", ex.getMessage());
			}
			
			errorCode = HttpStatus.CONFLICT;
		
		} else if(ex.getClass().equals(IllegalArgumentException.class) ||
				  ex.getClass().equals(MissingServletRequestParameterException.class)) {
			errorCode = HttpStatus.BAD_REQUEST;
		
		} else if(ex.getClass().equals(MethodArgumentTypeMismatchException.class)) {
			output.put("error", "Userid must be a numeric value");
			errorCode = HttpStatus.BAD_REQUEST;
			
		} else if(ex.getClass().equals(FailedLoginException.class)) {
			output.put("error", "Incorrect username or password");
			errorCode = HttpStatus.UNAUTHORIZED;
		
		} else if(ex.getClass().equals(ExpiredAuthKeyException.class) ||
				ex.getClass().equals(InvalidAuthKeyException.class)) {
			errorCode = HttpStatus.UNAUTHORIZED;
		
		} else {
			output.put("error", ex.getClass());
			errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		return new ResponseEntity<Map<String, Object>>(output, errorCode);
	}
}
