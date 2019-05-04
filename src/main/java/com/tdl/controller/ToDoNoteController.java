package com.tdl.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;


import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
//import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
//import com.svajukiux.controllers.CustomerController;
//import com.svajukiux.controllers.GameController;
import com.tdl.exception.InvalidFieldException;
import com.tdl.exception.ToDoNoteNotFoundException;
import com.tdl.model.ArrayResponsePojo;
import com.tdl.model.Order;
import com.tdl.model.ResponsePojo;
import com.tdl.model.ToDoNote;
import com.tdl.model.ToDoNoteDTO;
import com.tdl.model.User;
import com.tdl.service.ToDoNoteServiceImpl;

import org.modelmapper.ModelMapper;



@RestController
public class ToDoNoteController {
	
	private ModelMapper modelMapper = new ModelMapper();
	private List<ToDoNote> notes;
	
	/*
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) 
	{
	    return restTemplateBuilder
	       .setConnectTimeout(2000)
	       .setReadTimeout(2000)
	       .build();
	}
	*/
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private ToDoNoteServiceImpl toDoNoteService;
	
	// unchecked types
	@GetMapping("/todos") // booo cia reikes List<Resources<ToDoNoteDto>
	public ResponseEntity<?> getAllToDoNote(@RequestParam(value = "embed",required =false)String embed) throws ParseException, JsonParseException, JsonMappingException, IOException{
		List<ToDoNote> notes = new ArrayList<ToDoNote>();
		List<ToDoNoteDTO> notesDTO = new ArrayList<ToDoNoteDTO>();
		notesDTO = toDoNoteService.getAllToDoNoteDTO(); // turim notes su emailais jei ne embed=users galima toki ir grazinti
		//List<ToDoNote> allNotes = toDoNoteService.getAllToDoNote();
		
		// buildas yra kur docker failas mazdaug
		
		
			
		if(embed!=null && embed.equals("users")) {
			//ArrayList<String> emails = new ArrayList<String>();
			RestTemplate restTemplate = new RestTemplate();
			for(int i=0; i< notesDTO.size();i++) {
				ArrayList<String> emails = notesDTO.get(i).getUserEmails();
				if(!emails.isEmpty()) { // jei ne empty email ArrayList
					ArrayList<User> users = new ArrayList<User>();
					for(int j=0; j<emails.size(); j++) {
						final String uri = "http://193.219.91.103:1858/users/"+emails.get(j);
						ResponseEntity<String> result =null;
						int statusCode=0;
						ObjectMapper mapper = new ObjectMapper();
						//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
						try { // If user exists we will just add it to our ToDoNote
							 result = restTemplate.getForEntity(uri, String.class);
							 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
							 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
							 users.add(userResponse);
							
						}
						catch (HttpClientErrorException ex) {
							return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
					                .body(ex.getResponseBodyAsString());     
						}
						catch(RestClientException ex2) {
							if(ex2.getCause() instanceof ConnectException) {
								System.out.println(ex2.getCause());
								return new ResponseEntity<String>("\"Could not connect\"",HttpStatus.CONFLICT);
							}
						}
						
						// get is kito web serviso pagal emailus
					}
					ToDoNote toDoNote = convertToEntity(notesDTO.get(i),true);
					toDoNote.setUsers(users);
					notes.add(toDoNote);
					// konvertuoti dto i note ir pridet prie jo userius
				}
				ToDoNote toDoNote = convertToEntity(notesDTO.get(i),true);
				notes.add(toDoNote);
			
			}
			return new ResponseEntity<List<ToDoNote>>(notes,HttpStatus.OK);
			
			//return new Resources<>(noteResources);
		}
		
		else {
			
			
			return new ResponseEntity<List<ToDoNoteDTO>>(notesDTO,HttpStatus.OK);
			//return null;
		}
		
	}
	
	
/*	
	@GetMapping("/todos/{toDoNoteId}/users")
	public List<User> getNotesUsers(@PathVariable int toDoNoteId) {
		ToDoNote note = toDoNoteService.getToDoNoteById(toDoNoteId);
		//final String uriTest = "http://193.219.91.103:1858/";

	    //RestTemplate restTemplateTest = new RestTemplate();
	    //String result2 = restTemplateTest.getForObject(uriTest, String.class);

	  //  System.out.println(result2);
	    
		if(note==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		
		
		//RestTemplate restTemplate = new RestTemplate();
		//restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
       // SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
        //        .getRequestFactory();
        //rf.setReadTimeout(2000);
        //rf.setConnectTimeout(2000);
		//final String uri = "http://193.219.91.103:1858/users";
		
		//try {
			//HttpHeaders requestHeaders = new HttpHeaders();
			//requestHeaders.setContentType(MediaType.APPLICATION_JSON);
			//HttpEntity<String> entity = new HttpEntity<String>("parameters", requestHeaders);
			//ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
			/*ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET,entity, String.class); 
			Gson g = new Gson();
			ArrayResponsePojo testDTO = g.fromJson(result.getBody(), ArrayResponsePojo.class); 
			System.out.println(testDTO.getData()[0].getFirstName());
			return result;
			
			List<User> users = toDoNoteService.getAllNotesUsers(note);
			//ResponseEntity<ArrayResponsePojo> result = restTemplate.exchange(uri, HttpMethod.GET,null, new ParameterizedTypeReference<ArrayResponsePojo>(){}); 
			return users;
			
		//}
		//catch (HttpClientErrorException ex) {
			//return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
	         //       .body(ex.getResponseBodyAsString());
		//     return null;
		//}
		//Resource<ToDoNote> resource = new Resource<ToDoNote>(note);
		//Link linkToSelf =  linkTo(methodOn(this.getClass()).getToDoNoteById(toDoNoteId)).withSelfRel();
		//Link linkToAll =  linkTo(methodOn(this.getClass()).getAllToDoNote()).withRel("allTodos");
		//resource.add(linkToSelf);
		//resource.add(linkToAll);
		
		
	}
	
	@GetMapping("/todos/priority/{number}")
	public List<ToDoNote> getAllPriorityNotes(@PathVariable int number){
		ArrayList<ToDoNote> priorityNotes = new ArrayList<ToDoNote>();
		List<ToDoNote> allNotes = toDoNoteService.getAllToDoNote();
		
		
		for(Iterator<ToDoNote> it= allNotes.iterator(); it.hasNext();) {
			ToDoNote toDoNote = it.next();
			if(toDoNote.getPriority()==number) {
				priorityNotes.add(toDoNote);
			}
		}
		
		return priorityNotes;
	}
	
	@GetMapping("/todos/{toDoNoteId}")
	public Resource<?> getToDoNoteById(@PathVariable int toDoNoteId,@RequestParam(value = "embed",required =false)String embed) {
		ToDoNote note = toDoNoteService.getToDoNoteById(toDoNoteId);
		if(note==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		//Link linkToSelf =  linkTo(methodOn(this.getClass()).getToDoNoteById(toDoNoteId,"false")).withSelfRel();
		//Link linkToAll =  linkTo(methodOn(this.getClass()).getAllToDoNote("true")).withRel("allTodos");
		Link linkToFull =  linkTo(methodOn(this.getClass()).getNotesUsers(toDoNoteId)).withRel("users");
		if(embed!=null && embed.equals("users")) {
			Resource<ToDoNote> resource = new Resource<ToDoNote>(note);
			//resource.add(linkToAll);
			return resource;
			
		}
		else {
			ToDoNoteDTO noteDto = convertToDto(note);
			Resource<ToDoNoteDTO> resource = new Resource<ToDoNoteDTO>(noteDto);
			//resource.add(linkToSelf);
			resource.add(linkToFull);
			//resource.add(linkToAll);
			return resource;
		}
		
		//Link linkToSelf =  linkTo(methodOn(this.getClass()).getToDoNoteById(toDoNoteId,"false")).withSelfRel();
		//Link linkToAll =  linkTo(methodOn(this.getClass()).getAllToDoNote("labas")).withRel("allTodos");
		//resource.add(linkToSelf);
		//resource.add(linkToAll);
		
	}
	
	@GetMapping("/users")
	public ResponseEntity<?> getUsersFromOtherService() throws JsonParseException, JsonMappingException, IOException {
		final String uri = "http://friend:5000/users";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result =null;
		//int statusCode=0;
		ObjectMapper mapper = new ObjectMapper();
		
		try { // If user exists we will just add it to our ToDoNote
			 result = restTemplate.getForEntity(uri, String.class);
			 ArrayResponsePojo pojo = mapper.readValue(result.getBody(), ArrayResponsePojo.class);
			 //User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
			 //ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
			 List<User> users = new ArrayList<User>(pojo.getData());
			 //users 
			 return new ResponseEntity<List<User>>(users,HttpStatus.CREATED);
			 	

			//System.out.println("result" + result);
			
			
		}
		catch (HttpClientErrorException ex) {
			System.out.println("value"+ ex.getStatusCode().value());
			return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
	                .body(ex.getResponseBodyAsString());
		}
		
		
	}
	
	
	
	
	
	@GetMapping("/todos/{toDoNoteId}/users/{email}")
	public Resource<User> getUserByEmail(@PathVariable int toDoNoteId,@PathVariable String email) {
		ToDoNote note = toDoNoteService.getToDoNoteById(toDoNoteId);
		if(note==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		//Link linkToSelf =  linkTo(methodOn(this.getClass()).getUserByEmail(toDoNoteId,email)).withSelfRel();
		//Link linkToAll =  linkTo(methodOn(this.getClass()).getNotesUsers(toDoNoteId)).withRel("allNotes");
		
		//System.out.println("email: " + email);
		User user = toDoNoteService.getUserFromNote(toDoNoteId, email);
		if(user==null) {
			throw new ToDoNoteNotFoundException("User with email "+ email + " not found for this note"); // reiktu naujo exception kur UserNotFound
		}
		
		Resource<User> resource = new Resource<User>(user);
		
		//Link linkToSelf =  linkTo(methodOn(this.getClass()).getToDoNoteById(toDoNoteId,"false")).withSelfRel();
		//Link linkToAll =  linkTo(methodOn(this.getClass()).getAllToDoNote("labas")).withRel("allTodos");
		//resource.add(linkToSelf);
		//resource.add(linkToAll);
		return resource;
		
		
	}
	
	@PostMapping("/todos/{toDoNoteId}/users")
	public ResponseEntity<?> addUserToNote(@RequestBody User user,@PathVariable int toDoNoteId) throws JsonParseException, JsonMappingException, ConnectException, IOException{
		String email = user.getEmail();
		final String uri = "http://193.219.91.103:1858/users/"+email;
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result =null;
		int statusCode=0;
		ObjectMapper mapper = new ObjectMapper();
		//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
		
		//return null;
		
		try { // If user exists we will just add it to our ToDoNote
			 result = restTemplate.getForEntity(uri, String.class);
			 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
			 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
			 ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
			 if(toDoNote.checkIfUserExists(userToRespond)==true) {
					return new ResponseEntity<String>("\"User already in this note\"",HttpStatus.CONFLICT);
			 }
			 	else {
			 		toDoNoteService.getToDoNoteById(toDoNoteId).addUser(userToRespond);
			 		return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
			 	}

			//System.out.println("result" + result);
			//testing lag
			
		}
		
		catch (HttpClientErrorException ex) {
			System.out.println("value"+ ex.getStatusCode().value());
			statusCode=ex.getStatusCode().value();     
		}
		catch(RestClientException ex2) {
			if(ex2.getCause() instanceof ConnectException) {
				System.out.println(ex2.getCause());
				return new ResponseEntity<String>("\"Coudl not connect\"",HttpStatus.CONFLICT);
			}
		}
		
	
		
		
		try { // If user does not exist by the given email we can POST
			if(statusCode==404) {
				if(user.getEmail()==null || user.getFirstName()==null || user.getLastName()==null) {
					 return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
				}
				final String uriPost = "http://friend:5000/users";
				result= restTemplate.postForEntity(uriPost, user, String.class);
				System.out.println("result" + result);
				
				ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				toDoNoteService.getToDoNoteById(toDoNoteId).addUser(userToRespond);
				return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
			}
		}
			catch (HttpClientErrorException ex) {
				System.out.println("value"+ ex.getStatusCode().value());
				return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
		                .body(ex.getResponseBodyAsString());
			}
	// istryniau daug tu throws not sure if thats that good
		
		System.out.println("Rip");
		return null;
		
	}
	
	
	@PutMapping("/todos/{toDoNoteId}/users/{email}")
	public ResponseEntity<?> updateUser(@PathVariable int toDoNoteId, @PathVariable String email,@RequestBody User user) throws JsonParseException, JsonMappingException, IOException{
		ToDoNote todos = toDoNoteService.getToDoNoteById(toDoNoteId);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result =null;
		ObjectMapper mapper = new ObjectMapper();
		final String uriPut = "http://friend:5000/users/"+email; 
		if(user.getEmail()==null || user.getFirstName()==null || user.getLastName()==null) {
			return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
		}
		else {
			try {
			HttpEntity<User> userEntity = new HttpEntity<User>(user);
				result = restTemplate.exchange(uriPut,HttpMethod.PUT,userEntity, String.class);
				ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				for(int i=0; i< toDoNoteService.getAllToDoNote().size(); i++) {
					toDoNoteService.getAllToDoNote().get(i).updateUser(user, email); // updates info in all of the notes
				}
				return new ResponseEntity<User>(userResponse,HttpStatus.ACCEPTED);
			}
			catch (HttpClientErrorException ex) {
			
				//System.out.println("value"+ ex.getStatusCode().value());
				return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
		                .body(ex.getResponseBodyAsString());
			}
		}
		
		
		
	}
	
	@PatchMapping("/todos/{toDoNoteId}/users/{email}")
	public ResponseEntity<?> patchUser(@PathVariable int toDoNoteId, @PathVariable String email,@RequestBody User user) throws JsonParseException, JsonMappingException, IOException{
		ToDoNote todos = toDoNoteService.getToDoNoteById(toDoNoteId);
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		ResponseEntity<String> result =null;
		ObjectMapper mapper = new ObjectMapper();
		final String uriPut = "http://friend:5000/users/"+email;
		try {
			HttpEntity<User> userEntity = new HttpEntity<User>(user);
			result = restTemplate.exchange(uriPut,HttpMethod.PATCH,userEntity, String.class);
			ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
			 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
			 for(int i=0; i< toDoNoteService.getAllToDoNote().size(); i++) {
					toDoNoteService.getAllToDoNote().get(i).updateUser(user, email); // updates info in all of the notes
				}
			return new ResponseEntity<User>(userResponse,HttpStatus.ACCEPTED);
		}
		catch (HttpClientErrorException ex) {
			
			//System.out.println("value"+ ex.getStatusCode().value());
			return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
	                .body(ex.getResponseBodyAsString());
		}
	}
	
	
	// remove user only from note
		@DeleteMapping("/todos/{toDoNoteId}/users/{email}")
		public ResponseEntity<User> deleteUser(@PathVariable int toDoNoteId,@PathVariable String email){
			ToDoNote todos = toDoNoteService.getToDoNoteById(toDoNoteId);
			
			if(todos == null) {
				throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found. Cannot delete.");
			}
			
			User user = toDoNoteService.getUserFromNote(toDoNoteId, email);
			if(user==null) {
				throw new ToDoNoteNotFoundException("User with email "+ email + " not found for this note"); // reiktu naujo exception kur UserNotFound
			}
			
			//ToDoNoteDTO  noteDto = convertToDto(todos);
			toDoNoteService.removeUser(toDoNoteId,email);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}
	
	/*@GetMapping("/todos/{toDoNoteId}/users")
	public List<User> getNotesUsers(@PathVariable int toDoNoteId) {
		ToDoNote note = toDoNoteService.getToDoNoteById(toDoNoteId);
		if(note==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		
		return note.getUsers(); // arba kreiptis i toDoNoteService
		
		
	}
	*/
	/*
	@PostMapping("/todos/{toDoNoteId}/users") 
	public ResponseEntity <String> postUser(@RequestBody User user, @PathVariable int toDoNoteId, UriComponentsBuilder builder)throws HttpMessageNotReadableException, ParseException{
		
		RestTemplate restTemplate = new RestTemplate();
		final String uri = "http://193.219.91.103:1858/users";
		
		try {
			ResponseEntity<String> result = restTemplate.postForEntity(uri, user, String.class); 
			return result;
			
		}
		catch (HttpClientErrorException ex) {
			return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
	                .body(ex.getResponseBodyAsString());
		     
		}
		//if(toDoNote==null) {
		//	return ResponseEntity.noContent().build(); // cia reikia naujo exception ten body not found or smth
		//}
		
		//if(toDoNote.getDateToComplete()!=null && toDoNote.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
		//	throw new InvalidFieldException("Invalid Date");
		//}
		
		//HttpHeaders headers = new HttpHeaders();
		//headers.setLocation(builder.path("/todos/{id}").buildAndExpand(toDoNote.getId()).toUri());
		//return new ResponseEntity<ToDoNote>(toDoNote,headers, HttpStatus.CREATED);
	}
	
	
	
	@PostMapping("/todos") 
	public ResponseEntity<ToDoNoteDTO> addNote(@RequestBody ToDoNote newNoteDto, UriComponentsBuilder builder)throws HttpMessageNotReadableException, ParseException{
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		System.out.println(newNoteDto.getUsers());
		
		//toDoNote.us
		
		if(newNoteDto==null) {
			return ResponseEntity.noContent().build(); // cia reikia naujo exception ten body not found or smth
		}
		
		if(newNoteDto.getDateToComplete()!=null && newNoteDto.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
			throw new InvalidFieldException("Invalid Date");
		}
		
		//ToDoNote toDoNote = convertToEntity(newNoteDto,true);
		
		//toDoNote=toDoNoteService.addToDoNote(toDoNote);
		//newNoteDto.setId(toDoNote.getId());
		
		//HttpHeaders headers = new HttpHeaders();
		//headers.setLocation(builder.path("/todos/{id}").buildAndExpand(toDoNote.getId()).toUri());
		//return new ResponseEntity<ToDoNoteDTO>(newNoteDto,headers, HttpStatus.CREATED);
		return null;
	}
	/*
	@PutMapping("/todos")
	public ResponseEntity<ToDoNoteDTO> updateToDoNote(@Valid @RequestBody ToDoNoteDTO noteDto) throws ParseException{
		ToDoNote todos = toDoNoteService.getToDoNoteById(noteDto.getId());
		//ToDoNoteDto
		
		if(todos == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ noteDto.getId() + " not found");
		}
		
		if(noteDto.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
			throw new InvalidFieldException("Invalid Date");
		}
		
		if(noteDto.getName()==null) {
			throw new InvalidFieldException("Name is required");
		}
		todos.setName(noteDto.getName());
		
		
		todos.setDateToComplete(noteDto.getDateToComplete());
		
		
		todos.setDescription(noteDto.getDescription());
		
		
		todos.setPriority(noteDto.getPriority());
		
		todos.setCompleted(noteDto.isCompleted());
		
		toDoNoteService.updateToDoNote(todos);
		return new ResponseEntity<ToDoNoteDTO>(noteDto, HttpStatus.OK);
		
		
	}
	
	
	@PutMapping("/todos/{id}")
	public ResponseEntity<ToDoNoteDTO> updateToDoNote(@Valid @RequestBody ToDoNoteDTO noteDto, @PathVariable int id) throws ParseException{
		ToDoNote oldNote = toDoNoteService.getToDoNoteById(id);
		//System.out.println("sizee "+toDoNoteService.getAllToDoNote().size());
		
		if(oldNote == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ id + " not found");
		}
		
		if(noteDto.getDateToComplete()!=null && noteDto.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
			throw new InvalidFieldException("Invalid Date");
		}
		
		oldNote.setName(noteDto.getName());
		oldNote.setDateToComplete(noteDto.getDateToComplete());
		oldNote.setDescription(noteDto.getDescription());
		oldNote.setPriority(noteDto.getPriority());
		oldNote.setCompleted(noteDto.isCompleted());
		
		toDoNoteService.updateToDoNote(oldNote);
		noteDto.setId(id);
		return new ResponseEntity<ToDoNoteDTO>(noteDto, HttpStatus.OK);
	}
	
	@PatchMapping("/todos/{id}")
	public ResponseEntity<ToDoNoteDTO> partlyUpdateToDoNote(@RequestBody ToDoNoteDTO noteDto, @PathVariable int id){
		ToDoNote oldNote = toDoNoteService.getToDoNoteById(id);
		
		if(oldNote == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ id + " not found");
		}
		noteDto.setId(id);
		
		if(noteDto.getName()!= null){
			oldNote.setName(noteDto.getName());
		}
		
		if(noteDto.getDateToComplete()!= null){
			oldNote.setDateToComplete(noteDto.getDateToComplete());
		}
		
		if(noteDto.getDescription()!= null){
			oldNote.setDescription(noteDto.getDescription());
		}
		
		if(noteDto.getPriority()!= null){
			oldNote.setPriority(noteDto.getPriority());
		}
		
		if(noteDto.isCompleted()!= null){
			oldNote.setCompleted(noteDto.isCompleted());
		}
		
		toDoNoteService.updateToDoNote(oldNote);
		noteDto = convertToDto(oldNote); 
		return new ResponseEntity<ToDoNoteDTO>(noteDto, HttpStatus.OK);
	}	
	
	@DeleteMapping("/todos/{toDoNoteId}")
	public ResponseEntity<ToDoNoteDTO> deleteToDoNote(@PathVariable int toDoNoteId){
		ToDoNote todos = toDoNoteService.getToDoNoteById(toDoNoteId);
		
		if(todos == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found. Cannot delete.");
		}
		//ToDoNoteDTO  noteDto = convertToDto(todos);
		toDoNoteService.deleteToDoNote(toDoNoteId);
		return new ResponseEntity<ToDoNoteDTO>(HttpStatus.NO_CONTENT);
	}
	
	*/
	
	private ToDoNoteDTO convertToDto(ToDoNote note) {
	    ToDoNoteDTO noteDto = modelMapper.map(note, ToDoNoteDTO.class);
	    return noteDto;
	}
	
	private ToDoNote convertToEntity(ToDoNoteDTO noteDto, boolean newNote) throws ParseException {
        ToDoNote note = modelMapper.map(noteDto, ToDoNote.class);
        if(newNote==true) {
        ArrayList<User> users= new ArrayList<User>();
        note.setUsers(users);
        return note;
        }
        
        return null;
       // else {
	     //   if (noteDto.getId() != null) {
	      //      ToDoNote oldNote = toDoNoteService.getToDoNoteById(noteDto.getId());
	       //     note.setUsers(oldNote.getUsers());
	          
	       // }
       // }
       // return note;
    }
}


