package com.dmm.task.data.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Tasks implements Comparable<Tasks> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String name;
	private String text;
	private LocalDate date;
	private boolean done;
	
	//compareTo()オーバーライド
	// 日付で並び替えるため、LocalDateのcompareToを使う
	@Override
	public int compareTo(Tasks t) {
		return this.date.compareTo(t.date);
	}
}
