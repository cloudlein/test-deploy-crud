package com.example.testdeploycrud.repository;

import com.example.testdeploycrud.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
