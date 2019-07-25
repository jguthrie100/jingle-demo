package com.jingle.controllers;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(properties = "server.ssl.enabled=false")
public class UserSignUpIntegrationTest {
	
	@LocalServerPort
	private int port;
	
	@Mock
	private static UserController userController;
	
	@BeforeClass
	public static void initialiseRestAssuredMockMvcStandalone() {
		RestAssuredMockMvc.standaloneSetup(userController);
	}
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		RestAssured.port = port;
		RestAssured.useRelaxedHTTPSValidation();
	}

	@Test
	public void t1_testSuccessfulSignup() {
		
		given().
				param("username", "jingleSignUpTest").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jingleSignUp@bells.com").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(201).
		        contentType(ContentType.JSON).
		        body("id", isA(Integer.class)).
		        body("username", equalTo("jingleSignUpTest")).
		        body("firstName", equalTo("Jingle")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("jingleSignUp@bells.com")).
		        body("passHash", equalTo("Aabci+Zivg4ekxmRzI3Ipg=="));
	}
	
	@Test
	public void t2_testUnsuccessfulSignup_DuplicateUsername() {
		
		given().
				param("username", "jingleSignUpTest").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jinglex@bells.com").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(409).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Username already taken"));
	}
	
	@Test
	public void t3_testUnsuccessfulSignup_DuplicateEmail() {
		
		given().
				param("username", "jingleTestx").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jingleSignUp@bells.com").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(409).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Email address already taken"));
	}
	
	@Test
	public void t4_testUnsuccessfulSignup_MissingUsername() {
		
		given().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jingle0@bells.com").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'username' is not present"));
	}
	
	@Test
	public void t5_testUnsuccessfulSignup_MissingEmail() {
		
		given().
				param("username", "jingleTesta").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'email' is not present"));
	}
	
	@Test
	public void t6_testUnsuccessfulSignup_BlankUsername() {
		
		given().
				param("username", "").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jingle2@bells.com").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Username cannot be blank"));
	}
	
	@Test
	public void t7_testUnsuccessfulSignup_BlankEmail() {
		
		given().
				param("username", "userSignUpTest3").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "").and().
				param("password", "jingle12").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Email address cannot be blank"));
	}
	
	@Test
	public void t8_testUnsuccessfulSignup_ShortPassword() {
		
		given().
				param("username", "jingleSignUpTestPass").and().
				param("firstname", "Jingle").and().
				param("lastname", "Bells").and().
				param("email", "jinglepass@bells.com").and().
				param("password", "1234567").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/signup").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Password must be a minimum of 8 characters long"));
	}

}
