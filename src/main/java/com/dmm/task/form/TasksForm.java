package com.dmm.task.form;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class TasksForm {
	@Size(min = 1, max = 255)
	private String title;
	
	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;
	
	@Size(min = 1, max = 500)
	private String text;
	
	private boolean done;
}
