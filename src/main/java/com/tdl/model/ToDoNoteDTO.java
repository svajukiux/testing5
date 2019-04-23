package com.tdl.model;

import java.util.ArrayList;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonFormat;
@Relation(collectionRelation = "notes") 
public class ToDoNoteDTO {
	private Integer id;
	@NotNull(message = "Name may not be null")
	private String name;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date dateToComplete;
	private String description;
	private Integer priority;
	private Boolean completed;
	

	public Boolean getCompleted() {
		return completed;
	}

	public ToDoNoteDTO() {
		super();
	}

	public ToDoNoteDTO(Integer id, String name, Date dateToComplete, String description,Integer priority, Boolean completed) {
		super();
		this.id = id;
		this.name = name;
		this.dateToComplete = dateToComplete;
		this.description = description;
		this.priority = priority;
		this.completed = completed;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateToComplete() {
		return dateToComplete;
	}

	public void setDateToComplete(Date dateToComplete) {
		this.dateToComplete = dateToComplete;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Boolean isCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
	
}
