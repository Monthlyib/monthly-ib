package com.monthlyib.server.domain.user.repository;

import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.entity.UserImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    private final UserImageJpaRepository userImageJpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable);
    }

    @Override
    public UserImage saveUserImage(UserImage userImage) {
        return userImageJpaRepository.save(userImage);
    }

    @Override
    public List<UserImage> findAllUserImage(Long userId) {
        return userImageJpaRepository.findAllByUserId(userId);
    }

    @Override
    public void deleteAllUserImage(Long userId) {
        userImageJpaRepository.deleteAllByUserId(userId);
    }
}
