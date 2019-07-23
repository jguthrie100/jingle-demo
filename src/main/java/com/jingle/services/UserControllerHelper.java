package com.jingle.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
	
	@Autowired
	private AuthService authService;
	
	@Autowired
	UserControllerHelper(UserRepository userRepository, AuthService authService) {
		this.userRepository = userRepository;
		this.authService = authService;
	}
	
	/**
	 * Save user to database
	 */
	public ResponseEntity<User> saveUser(User user) {
		
		Map<String, Object> output = new HashMap<String, Object>();
		
		byte[] passwordHash;
		
		try {
			passwordHash = authService.getPasswordHash(new String(user.getPassHash()));
			user.setPasswordHash(passwordHash);
		
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		User savedUser;
		
		try {
			savedUser = userRepository.save(user);
			
		} catch (IllegalArgumentException e) {
			output.put("error", e.getMessage());
			return new ResponseEntity(output, HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<User>(savedUser, HttpStatus.CREATED);
	}

	/**
	 * Login with username and password - returns Authentication Key
	 */
	public ResponseEntity<Map<String, Object>> loginUser(String username, String password) {
		
		Map<String, Object> output = new HashMap<String, Object>();
		
		String authKey;
		Long userId;
		
		try {
			authKey = getAuthKey(username, password);
			userId = userRepository.findByUsername(username).get().getId();
			
		} catch (IllegalArgumentException e) {
			// Username or password is incorrect - unauthorized
			output.put("error", e.getMessage());
			return new ResponseEntity<Map<String, Object>>(output, HttpStatus.UNAUTHORIZED);
		
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return new ResponseEntity<Map<String, Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		output.put("id", userId);
		output.put("authKey", authKey);
		
		return new ResponseEntity<Map<String, Object>>(output, HttpStatus.OK);
	}
	
	/**
	 * Edit existing user - returns updated user
	 */
	public ResponseEntity<User> editUser(long userId, String authKey, String username, String firstName, String lastName, String emailAddress, String password) {
		Map<String, Object> output = new HashMap<String, Object>();
		
		User user;
		User updatedUser;
		
		try {
			user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
			
			authService.isValidAuthKey(authKey, user);
			
			if(username != null) user.setUsername(username);
			
			if(firstName != null) user.setFirstName(firstName);
			
			if(lastName != null) user.setLastName(lastName);
			
			if(emailAddress != null) user.setEmailAddress(emailAddress);
			
			if(password != null) user.setPasswordHash(authService.getPasswordHash(password));
			
			updatedUser = userRepository.save(user);
			
		} catch (ExpiredAuthKeyException | InvalidAuthKeyException e) {
			output.put("error", e.getMessage());
			return new ResponseEntity(output, HttpStatus.UNAUTHORIZED);
		
		} catch (IllegalArgumentException e) {
			output.put("error", e.getMessage());
			return new ResponseEntity(output, HttpStatus.BAD_REQUEST);
		
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<User>(updatedUser, HttpStatus.OK);
	}
	
	/**
	 * Deletes a user from the database - returns whether successful or not
	 */
	public ResponseEntity<Map<String, Object>> deleteUser(long userId, String authKey) {
		Map<String, Object> output = new HashMap<String, Object>();
		
		User userToDelete = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
		
		try {
			authService.isValidAuthKey(authKey, userToDelete);
			userRepository.deleteById(userId);
			
		} catch (ExpiredAuthKeyException | InvalidAuthKeyException e) {
			output.put("success", false);
			output.put("error", e.getMessage());
			
			return new ResponseEntity<Map<String, Object>>(output, HttpStatus.UNAUTHORIZED);
		
		} catch (IllegalArgumentException e) {
			output.put("success", false);
			output.put("error", e.getMessage());
			
			return new ResponseEntity<Map<String, Object>>(output, HttpStatus.BAD_REQUEST);
		}
		
		output.put("success", true);
		
		return new ResponseEntity<Map<String, Object>>(output, HttpStatus.OK);
	}
	
	/**
	 * Returns a User from the database
	 */
	public ResponseEntity<User> getUser(Long userId, String username) {
		
		Map<String, Object> output = new HashMap<String, Object>();
		
		User user;
		
		try {
			if(userId != null) {
				user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User with userid (" + userId + ") doesn't exist"));
			
			} else if(username != null) {
				user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User with username (" + username + ") doesn't exist"));
			
			} else {
				throw new IllegalArgumentException("Either a userid or username must be provided");
			}
			
		} catch (IllegalArgumentException e) {
			output.put("error", e.getMessage());
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	/**
	 * Returns an authorization key if the username matches the password
	 */
	private String getAuthKey(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Incorrect username or password"));
		
		if(Arrays.equals(user.getPassHash(), authService.getPasswordHash(password))) {
			return authService.newAuthKey(user.getId());
		
		} else {
			throw new IllegalArgumentException("Incorrect username or password");
		}
	}
}
