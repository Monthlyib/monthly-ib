package com.monthlyib.server.domain.user.repository;

import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.entity.UserImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    User save(User user);

    Optional<User> findById(Long id);

    Page<User> findAll(Pageable pageable);

    UserImage saveUserImage(UserImage userImage);

    List<UserImage> findAllUserImage(Long userId);

    void deleteAllUserImage(Long userId);
}
