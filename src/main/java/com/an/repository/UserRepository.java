package com.an.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.an.entity.User;


public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
}
