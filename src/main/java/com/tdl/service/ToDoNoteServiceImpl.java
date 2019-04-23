package com.tdl.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdl.model.ArrayResponsePojo;
import com.tdl.model.ToDoNote;
import com.tdl.model.User;

@Component
public class ToDoNoteServiceImpl implements ToDoNoteService{
	
	private static List<ToDoNote> todos = new ArrayList<>();
	
	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Calendar c = Calendar.getInstance();
		//Date d= new Date();
		//c.add(Calendar.DATE, 3);
		ToDoNote workout1 = new ToDoNote(1, "Monday workout",  new Date(), "Leg Day", 1, false);
		ToDoNote workout2 = new ToDoNote(2, "Just workout",  new Date()   , "Full Body Day", 2, false);
		ToDoNote workout3 = new ToDoNote(3, "An workout",  new Date()  , "Sleep Day", 2, false);
		
		try {
			 workout1 = new ToDoNote(1, "Monday workout",  (Date)dateFormat.parse("2019-03-22")   , "Leg Day", 1, false);
			 workout2 = new ToDoNote(2, "Just workout",  (Date)dateFormat.parse("2019-03-20")   , "Full Body Day", 2, false);
			 workout3 = new ToDoNote(3, "An workout",  (Date)dateFormat.parse("2019-03-19")  , "Sleep Day", 2, false);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RestTemplate restTemplate = new RestTemplate();
		
		final String uri = "http://friend:5000/"; //2nd service database fill
		
		try {
			restTemplate.getForEntity(uri, String.class); 
			//return;
			
		}
		catch (HttpClientErrorException ex) {
			ex.printStackTrace();
		     
		}
		
		final String uriGet = "http://friend:5000/users";
		try {
			ResponseEntity<String> startingUsers =restTemplate.getForEntity(uriGet, String.class); 
			ObjectMapper mapper = new ObjectMapper();
			ArrayResponsePojo response = mapper.readValue(startingUsers.getBody(),ArrayResponsePojo.class);
			ArrayList<User> users = response.getData(); // at start there are 3 users
			workout1.addUser(users.get(0));
			workout2.addUser(users.get(1));
			workout3.addUser(users.get(2));
		}
		
		catch (HttpClientErrorException | IOException ex) {
			ex.printStackTrace();
		     
		}
		
		
		todos.add(workout1);
		todos.add(workout2);
		todos.add(workout3);
	}
	
	@Override
	public List<ToDoNote> getAllToDoNote() {
		return todos;
	}

	@Override
	public ToDoNote getToDoNoteById(Integer id) {
		for(ToDoNote toDoNote : todos) {
			if(toDoNote.getId() == id) {
				return toDoNote;
			}
		}
		return null;
	}

	@Override
	public ToDoNote addToDoNote(ToDoNote toDoNote) {
		
		
		ToDoNote tempNote = todos.get(todos.size()-1);
		int id = tempNote.getId()+1;
		toDoNote.setId(id);
		todos.add(toDoNote);
		return toDoNote;
		
	}

	@Override
	public void updateToDoNote(ToDoNote toDoNote) {
		for(ToDoNote oldToDoNote : todos) {
			if(oldToDoNote.getId() == toDoNote.getId()) {
				oldToDoNote.setName(toDoNote.getName());
				oldToDoNote.setDateToComplete(toDoNote.getDateToComplete());
				oldToDoNote.setDescription(toDoNote.getDescription());
				oldToDoNote.setPriority(toDoNote.getPriority());
				oldToDoNote.setCompleted(toDoNote.isCompleted());
			}
		}
	}

	@Override
	public void deleteToDoNote(Integer id) {
		for(Iterator<ToDoNote> it= todos.iterator(); it.hasNext();) {
			ToDoNote toDoNote = it.next();
			if(toDoNote.getId() == id) {
				it.remove();
				break;
			}
		}
		
	}
	public List<User> getAllNotesUsers(ToDoNote toDoNote){ // pakeisti kad imtu id
		return toDoNote.getUsers();
	
	}
	
	public User getUserFromNote(int noteId, String email) {
		ToDoNote note = this.getToDoNoteById(noteId);
		ArrayList<User> users = note.getUsers();
		for(int i=0; i<users.size(); i++) {
			User tempUser = users.get(i);
			if(tempUser.getEmail().equals(email)) {
				return tempUser;
			}
		}
		return null;
	}
	
	public void removeUser(int noteId, String email) {
		ArrayList <User> users = this.getToDoNoteById(noteId).getUsers();
		for(Iterator<User> it= users.iterator(); it.hasNext();) {
			User user = it.next();
			if(user.getEmail().equals(email)) {
				it.remove();
				break;
				
			}
		}
		
		
	}

}
