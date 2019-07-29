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
	
	private String authKey;
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
		userId =	given().
							param("username", "userEditTest" + i).and().
							param("firstname", "Jingle").and().
							param("lastname", "Bells").and().
							param("email", "userEditTest@bells.com" + i).and().
							param("password", "jingle123").and().
							header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup").
					then().
							extract().
							jsonPath().getInt("id");
					
					// Prepare duplicate
					given().
						param("username", "userEditTestDup" + i).and().
						param("firstname", "Jingle").and().
						param("lastname", "Bells").and().
						param("email", "userEditTestDup@bells.com" + i).and().
						param("password", "jingle123").and().
						header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup");
		
		authKey = 	given().
						param("username", "userEditTest" + i).and().
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
		prepare(1);
		
		given().
				param("userid", userId).and().
				param("username", "userEditTestNew1").and().
				param("firstname", "editNew").and().
				header("Auth-Key", authKey).
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("username", equalTo("userEditTestNew1")).
		        body("firstName", equalTo("editNew")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("userEditTest@bells.com1")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t2_testSuccessfulEdit_SameEmail() {
		prepare(2);
		
		given().
				param("userid", userId).and().
				param("username", "userEditTestNew").and().
				param("email", "userEditTest@bells.com2").and().
				param("firstname", "editNew").and().
				header("Auth-Key", authKey).
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
		        body("emailAddress", equalTo("userEditTest@bells.com2")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t3_testSuccessfulEdit_NewEmail() {
		prepare(3);
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew@bells.com3").and().
				param("firstname", "editNew").and().
				header("Auth-Key", authKey).
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(200).
		        contentType(ContentType.JSON).
		        body("id", equalTo(userId)).
		        body("username", equalTo("userEditTest3")).
		        body("firstName", equalTo("editNew")).
		        body("lastName", equalTo("Bells")).
		        body("emailAddress", equalTo("userEditTestNew@bells.com3")).
		        body("passHash", equalTo("OIiN4IgSDnWl5rCYEryyBw=="));
	}
	
	@Test
	public void t4_testUnsuccessfulEdit_DuplicateUsername() {
		prepare(4);
		
		given().
				param("userid", userId).
				param("username", "userEditTestDup4").and().
				header("Auth-Key", authKey).
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
		prepare(5);
		prepare(500);
		
		given().
				param("userid", userId).and().
				param("email", "userEditTest@bells.com5").and().
				header("Auth-Key", authKey).
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
		prepare(6);
		
		given().
				param("userid", 10000).and().
				param("email", "userEditTestNew@bells.com6").and().
				header("Auth-Key", authKey).
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
		prepare(7);
		
		// Blank userid
		given().
				param("userid", "").and().
				header("Auth-Key", authKey).
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
		prepare(8);
		
		given().
				header("Auth-Key", authKey).
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
		prepare(9);
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew@bells.com9").and().
				header("Auth-Key", authKey + "_").
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
		prepare(10);
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew@bells.com10").and().
				header("Auth-Key", "").
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(401).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Invalid auth key"));
	}
	
	@Test
	public void t11_testUnsuccessfulEdit_MissingAuthKey() {
		prepare(11);
		
		given().
				param("userid", userId).and().
				param("email", "userEditTestNew@bells.com11").and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				put("/edit").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Missing request header 'Auth-Key' for method parameter of type String"));
	}
}

