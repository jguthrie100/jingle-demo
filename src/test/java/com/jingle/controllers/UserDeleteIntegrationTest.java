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
							param("username", "userDeleteTest" + i).and().
							param("firstname", "Jingle").and().
							param("lastname", "Bells").and().
							param("email", "userDeleteTest@jingle.com" + i).and().
							param("password", "jingle123").and().
							header("Content-Type", "application/x-www-form-urlencoded").
					when().
							post("/signup").
					then().
							extract().
							jsonPath().getInt("id");
		
		authKey = 	given().
						param("username", "userDeleteTest" + i).and().
						param("password", "jingle123").and().
						header("Content-Type", "application/x-www-form-urlencoded").
					when().
						post("/login").
					then().
						extract().
						jsonPath().getString("authKey");
		
		i++;
	}

	@Test
	public void t1_testUnSuccessfulDelete_MissingUserId() {
		prepare(1);
		
		given().
				header("Auth-Key", authKey).
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
		prepare(2);
		
		given().
				param("userid", "").and().
				header("Auth-Key", authKey).
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
		prepare(3);
		
		given().
				param("userid", 100000).and().
				header("Auth-Key", authKey).
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
		prepare(4);
		
		given().
				param("userid", userId).and().
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("Missing request header 'Auth-Key' for method parameter of type String"));
	}
	
	@Test
	public void t5_testUnSuccessfulDelete_BlankAuthKey() {
		prepare(5);
		
		// give generic invalid auth key message (for security purposes)
		given().
				param("userid", userId).and().
				header("Auth-Key", "").
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
		prepare(6);
		
		given().
				param("userid", userId).and().
				header("Auth-Key", authKey).
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
		prepare(7);
		
		given().
				param("userid", userId).and().
				header("Auth-Key", authKey).
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete");
		
		given().
				param("userid", userId).and().
				header("Auth-Key", authKey).
				header("Content-Type", "application/x-www-form-urlencoded").
		when().
				delete("/delete").
		then().
		        statusCode(400).
		        contentType(ContentType.JSON).
		        body("error", equalTo("User with userid (" + userId + ") doesn't exist"));
	}
}

