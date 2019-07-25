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
public class UserEditIntegrationTest {
	
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
							param("username", "userEditTest").and().
							param("firstname", "Jingle").and().
							param("lastname", "Bells").and().
							param("email", "userEditTest@bells.com").and().
							param("password", "jingle123").and().
							header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup").
					then().
							extract().
							jsonPath().getInt("id");
					
					// Prepare duplicate
					given().
						param("username", "userEditTest2").and().
						param("firstname", "Jingle").and().
						param("lastname", "Bells").and().
						param("email", "userEditTest2@bells.com").and().
						param("password", "jingle123").and().
						header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup");
		
		authKey = 	given().
						param("username", "userEditTest").and().
						param("password", "jingle123").and().
						header("Content-Type", "application/x-www-form-urlencoded").
					when().
						post("/login").
					then().
						extract().
						jsonPath().getString("authKey");
	}

	@Test
	public void t1_testSuccessfulEdit_MissingEmail() {
		
		given().
				param("userid", userId).and().
				param("username", "userEditTestNew").and().
				param("firstname", "editNew").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("username", equalTo("userEditTestNew")).
		        body("firstName", equalTo("editNew")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("userEditTest@bells.com")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t2_testSuccessfulEdit_SameEmail() {
		
		given().
				param("userid", userId).and().
				param("username", "userEditTestNew").and().
				param("email", "userEditTest@bells.com").and().
				param("firstname", "editNew").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("username", equalTo("userEditTestNew")).
		        body("firstName", equalTo("editNew")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("userEditTest@bells.com")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t2_testSuccessfulEdit_NewEmail() {
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew@bells.com").and().
				param("firstname", "editNew").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("username", equalTo("userEditTestNew")).
		        body("firstName", equalTo("editNew")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("userEditTestNew@bells.com")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t4_testUnsuccessfulEdit_DuplicateUsername() {
		
		given().
				param("userid", userId).
				param("username", "userEditTest2").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(409).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Username already taken"));
	}
	
	@Test
	public void t5_testUnsuccessfulEdit_DuplicateEmail() {
		
		given().
				param("userid", userId).and().
				param("email", "userEditTest2@bells.com").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(409).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Email address already taken"));
	}
	
	@Test
	public void t6_testUnsuccessfulEdit_NonExistentUserId() {
		
		given().
				param("userid", 10000).and().
				param("email", "userEditTestNew3@bells.com").and().
				param("authkey", authKey + "_").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("User with userid (10000) doesn't exist"));
	}
	
	@Test
	public void t7_testUnsuccessfulEdit_BlankUserId() {
		// Blank userid
		given().
				param("userid", "").and().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
				statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Userid must be a numeric value"));
	}
	
	@Test
	public void t8_testUnsuccessfulEdit_MissingUserId() {
		given().
				param("authkey", authKey).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required long parameter 'userid' is not present"));
	}
	
	@Test
	public void t9_testUnsuccessfulEdit_InvalidAuthKey() {
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew2@bells.com").and().
				param("authkey", authKey + "_").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Invalid auth key"));
	}
	
	@Test
	public void t10_testUnsuccessfulEdit_BlankAuthKey() {
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew2@bells.com").and().
				param("authkey", "").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Invalid auth key"));
	}
	
	@Test
	public void t10_testUnsuccessfulEdit_MissingAuthKey() {
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew2@bells.com").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Required String parameter 'authkey' is not present"));
	}
}

