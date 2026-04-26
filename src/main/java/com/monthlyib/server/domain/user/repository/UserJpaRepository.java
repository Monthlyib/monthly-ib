package com.monthlyib.server.domain.user.repository;

import com.monthlyib.server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByEmailIgnoreCase(String email);

    Page<User> findAll(Pageable pageable);

    @Query("select lower(u.email) from User u group by lower(u.email) having count(u) > 1")
    List<String> findDuplicateNormalizedEmails();
}
