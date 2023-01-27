package com.dmm.task.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dmm.task.data.entity.Users;

public interface UsersRepository extends JpaRepository<Users, String> {
	@Query("select a.roleName from Users a where a.name = :name")
	String findByUser(@Param("name") String name);
}
