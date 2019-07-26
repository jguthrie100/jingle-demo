package com.jingle.controllers;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "server.ssl.enabled=false")
public class UserLoginIntegrationTest {
	
	private Integer userId;
	
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
	
	public void prepare(int i) {
		userId = 	given().
							param("username", "userLoginTest" + i).and().
							param("firstname", "Jingle").and().
							param("lastname", "Bells").and().
							param("email", "userLoginTest@bells.com" + i).and().
							param("password", "jingle123").and().
							header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup").
					then().
					        extract().
					        jsonPath().getInt("id");
	}

	@Test
	public void testSuccessfulLogin() {
		prepare(1);
		
		given().
				param("username", "userLoginTest1").and().
				param("password", "jingle123").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("authKey", matchesPattern("^[A-Z0-9_]{30}$"));
	}
	
	@Test
	public void testUnsuccessfulLogin_WrongUsername() {

		given().
				param("username", "userLoginTestX").and().
				param("password", "jingle123").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Incorrect username or password"));
	}
	
	@Test
	public void testUnsuccessfulLogin_WrongPassword() {
		prepare(3);
		
		given().
				param("username", "userLoginTest3").and().
				param("password", "jingle1234").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Incorrect username or password"));
	}
	
	@Test
	public void testUnsuccessfulLogin_MissingUsername() {
		
		given().
				param("password", "jingle123").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'username' is not present"));
	}
	
	@Test
	public void testUnsuccessfulLogin_MissingPassword() {
		prepare(5);
		
		given().
				param("username", "jingleTest5").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'password' is not present"));
	}
	
	@Test
	public void testUnsuccessfulLogin_BlankUsername() {
		// Blank username should just give generic incorrect password error
		given().
				param("username", "").and().
				param("password", "jingle123").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Incorrect username or password"));
	}
	
	@Test
	public void testUnsuccessfulLogin_BlankPassword() {
		prepare(7);
		
		// Blank password should just give generic incorrect password error
		given().
				param("username", "userLoginTest7").and().
				param("password", "").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				post("/login").
		then().
				statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Incorrect username or password"));
	}

}
