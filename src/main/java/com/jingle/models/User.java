package com.jingle.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Models a User object
 *
 */
@Entity
public class User {
	
	@Id
	@Column(unique = true)
    @GeneratedValue(strategy=GenerationType.AUTO)
	@NotNull
	private Long id;
	
	@Column(unique = true)
	private String username;
	
	private String firstName;
	private String lastName;
	
	@Column(unique = true)
	private String emailAddress;
	
	private byte[] passwordHash;
	
	protected User() {};
	
	public User(String username, String firstName, String lastName, String emailAddress, byte[] passwordHash) {
		setUsername(username);
		setFirstName(firstName);
		setLastName(lastName);
		setEmailAddress(emailAddress);
		setPasswordHash(passwordHash);
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public void setUsername(String username) {
		if(username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Username cannot be blank");
		}
		
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getFirstName() {
		return this.firstName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setEmailAddress(String emailAddress) {
		if(emailAddress == null || emailAddress.isEmpty()) {
			throw new IllegalArgumentException("Email address cannot be blank");
		}
		
		this.emailAddress = emailAddress;
	}
	
	public String getEmailAddress() {
		return this.emailAddress;
	}
	
	public void setPasswordHash(byte[] passHash) {
		this.passwordHash = passHash;
	}
	
	public byte[] getPassHash() {
		return this.passwordHash;
	}
}
