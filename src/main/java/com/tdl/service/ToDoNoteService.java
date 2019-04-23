package com.tdl.service;

import java.util.List;

import com.tdl.model.*;

public interface ToDoNoteService {
	
	public List<ToDoNote> getAllToDoNote();
	
	public ToDoNote getToDoNoteById(Integer id);
	
	public ToDoNote addToDoNote(ToDoNote toDoNote);
	
	public void updateToDoNote(ToDoNote toDoNote);
	
	public void deleteToDoNote(Integer id);
	
	public List<User> getAllNotesUsers(ToDoNote toDoNote);
}
