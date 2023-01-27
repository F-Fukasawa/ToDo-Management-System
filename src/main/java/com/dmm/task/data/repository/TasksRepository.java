package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dmm.task.data.entity.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Long>{
	@Query("select a from Tasks a where a.date between :from and :to and a.name = :name")
	List<Tasks> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("name") String name);
	
	@Query("select a from Tasks a where a.date between :from and :to")
	List<Tasks> findByDateBetweenAll(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
