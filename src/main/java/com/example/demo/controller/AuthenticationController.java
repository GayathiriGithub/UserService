package com.example.demo.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.User;
import com.example.demo.service.UserServiceImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("auth/v1")
public class AuthenticationController 
{
	@Autowired
    private DiscoveryClient discoveryClient;

	private Map<String, String> map = new HashMap();
	
	private UserServiceImpl userServiceImpl;

	@Autowired
	public AuthenticationController(UserServiceImpl userServiceImpl) {
		super();
		this.userServiceImpl = userServiceImpl;
	}
	@GetMapping("/")
	public String serviceStarted()
	{
		return "Authentication Service Started";
	}
	
	public String generateToken(String username, String password) throws ServletException, Exception
	{
		String jwtToken ="";
		if(username == null || password == null)
		{
			throw new ServletException("Please send valid username and password");
		}
		//validate the user against DB values
		boolean flag = userServiceImpl.validateUser(username, password);
		if(!flag)
		{
			throw new ServletException("Invalid credentials");
		}
		else
		{
			jwtToken = Jwts.builder().setSubject(username)
					.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+300000))
					.signWith(SignatureAlgorithm.HS256, "secret key")
					.compact();
		}
		return jwtToken;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> doLogin(@RequestBody User user)
	{
		try
		{
			String jwtToken = generateToken(user.getUsername(), user.getPassword());
			map.put("message", "User successfully logged in!");
			map.put("token", jwtToken);
		}
		catch(Exception e)
		{
			map.put("message", e.getMessage());
			map.put("token", null);
			return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@PostMapping(value="/addUser", consumes="application/json; charset=utf-8")
	public ResponseEntity<?> addUser(@RequestBody User user)
	{
		if(userServiceImpl.addUser(user)!=null)
		{
			return new ResponseEntity<User>(user, HttpStatus.CREATED);
		}
		return new ResponseEntity<String>("Data not inserted!", HttpStatus.CONFLICT);
	}
	
	
	@PostMapping(value="/getToken", consumes="application/json; charset=utf-8")
	public ResponseEntity<?> getToken(@RequestBody User user)
	{
		if(userServiceImpl.addUser(user)!=null)
		{
			return new ResponseEntity<User>(user, HttpStatus.CREATED);
		}
		return new ResponseEntity<String>("Data not inserted!", HttpStatus.CONFLICT);
	}
	
	
}