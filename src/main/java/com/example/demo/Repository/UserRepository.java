package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByEmail(String email);

    User findByRole(String role);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> findByActivationToken(String activationToken);

    @Override
    Page<User> findAll(Pageable pageable);
}