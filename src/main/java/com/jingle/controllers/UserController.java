package com.jingle.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.jingle.exceptions.ExpiredAuthKeyException;
import com.jingle.exceptions.InvalidAuthKeyException;
import com.jingle.models.User;
import com.jingle.services.UserControllerHelper;

@RestController
public class UserController {
	
	@Autowired
	private UserControllerHelper apiHelper;
	
	@Autowired
	UserController(UserControllerHelper apiHelper) {
		this.apiHelper = apiHelper;
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest req, Exception ex) {
		return apiHelper.exceptionHandler(req, ex);
	}
	
	/**
	 * Save a new user to the database
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<User> signUp(@RequestParam(value = "username") String username,
                                       @RequestParam(value = "firstname") String firstName,
                                       @RequestParam(value = "lastname") String lastName,
                                       @RequestParam(value = "email") String emailAddress,
                                       @RequestParam(value = "password") String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		User userData = new User(username, firstName, lastName, emailAddress, password.getBytes());
		
		return apiHelper.saveUser(userData);
	}
	
	/**
	 * Login and retrieve an authentication key
	 */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> login(@RequestParam(value = "username") String username,
                                                     @RequestParam(value = "password") String password) throws NoSuchAlgorithmException, InvalidKeySpecException, FailedLoginException {
		
		return apiHelper.loginUser(username, password);
	}
	
	/**
	 * Update a specific User object using the given parameter values.
	 * All params are optional except for the userid and authkey params 
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.PUT)
	public ResponseEntity<User> editUser(@RequestParam(value = "userid") long userId,
										 @RequestParam(value = "authkey") String authKey,
										 @RequestParam(value = "username", required = false) String username,
										 @RequestParam(value = "firstname", required = false) String firstName,
										 @RequestParam(value = "lastname", required = false) String lastName,
										 @RequestParam(value = "email", required = false) String emailAddress,
										 @RequestParam(value = "password", required = false) String password) throws NoSuchAlgorithmException, InvalidKeySpecException, ExpiredAuthKeyException, InvalidAuthKeyException {
		
		return apiHelper.editUser(userId, authKey, username, firstName, lastName, emailAddress, password);
	}
	
	/**
	 * Delete a specific User object from the database
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam(value = "userid") long userId,
									   		  @RequestParam(value = "authkey") String authKey) throws ExpiredAuthKeyException, InvalidAuthKeyException {
		
		return apiHelper.deleteUser(userId, authKey);
	}
	
	/**
	 * Return a specific User object
	 * Requires either a userid or a username to be passed in as a parameter (with userid
	 * taking preference if both are passed)
	 */
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public ResponseEntity<User> getUser(@RequestParam(value = "userid", required = false) Long userId,
										@RequestParam(value = "username", required = false) String username) {
		
		return apiHelper.getUser(userId, username);
	}

	/**
	 * Default path
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<String> displayDefault() {
		
		return new ResponseEntity<String>("Please visit http://www.github.com/jguthrie100/jingle-demo for information about this API", HttpStatus.OK);
	}
}
