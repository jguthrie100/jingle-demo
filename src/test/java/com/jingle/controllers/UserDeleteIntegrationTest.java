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
public class UserDeleteIntegrationTest {
	
	private static String authKey;
	private static Integer userId;
	
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
	
	@BeforeClass
	public static void prepare() {
		userId =	given().
							param("username", "userDeleteTest").and().
							param("firstname", "Jingle").and().
							param("lastname", "Bells").and().
							param("email", "userDeleteTest@jingle.com").and().
							param("password", "jingle123").and().
							header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup").
					then().
							extract().
							jsonPath().getInt("id");
		
		authKey = 	given().
						param("username", "userDeleteTest").and().
						param("password", "jingle123").and().
						header("Content-Type", "application/x-www-form-urlencoded").
					when().
						post("/login").
					then().
						extract().
						jsonPath().getString("authKey");
	}

	@Test
	public void t1_testUnSuccessfulDelete_MissingUserId() {
		
		given().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required long parameter 'userid' is not present"));
	}
	
	@Test
	public void t2_testUnSuccessfulDelete_BlankUserId() {
		
		given().
				param("userid", "").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Userid must be a numeric value"));
	}
	
	@Test
	public void t3_testUnSuccessfulDelete_InvalidUserId() {
		
		given().
				param("userid", 100000).and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("User with userid (100000) doesn't exist"));
	}
	
	@Test
	public void t4_testUnSuccessfulDelete_MissingAuthKey() {
		
		given().
				param("userid", userId).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'authkey' is not present"));
	}
	
	@Test
	public void t5_testUnSuccessfulDelete_BlankAuthKey() {
		// give generic invalid auth key message (for security purposes)
		given().
				param("userid", userId).and().
				param("authkey", "").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Invalid auth key"));
	}
	
	@Test
	public void t6_testSuccessfulDelete() {
		
		given().
				param("userid", userId).and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("success", equalTo(true));
	}
	
	@Test
	public void t7_testUnSuccessfulDelete_SameId() {
		
		given().
				param("userid", userId).and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("User with userid (" + userId + ") doesn't exist"));
	}
}

