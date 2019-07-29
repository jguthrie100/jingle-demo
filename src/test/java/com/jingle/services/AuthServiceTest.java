package com.jingle.services;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.junit.Test;

public class AuthServiceTest {
	
	private AuthService auth = new AuthService();

	@Test
	public void testGetPasswordHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
		assertEquals(Arrays.toString(new int[] {1, -90, -36, -117, -26, 98, -66, 14, 30, -109, 25, -111, -52, -115, -56, -90}), Arrays.toString(auth.getPasswordHash("jingle12")));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetPasswordHash_LessThanMinLength() throws NoSuchAlgorithmException, InvalidKeySpecException {
		assertEquals("fail", auth.getPasswordHash("jingle1"));
	}

}
