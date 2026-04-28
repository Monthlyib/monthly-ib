package com.monthlyib.server.domain.user.repository;

import com.monthlyib.server.domain.user.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserImageJpaRepository extends JpaRepository<UserImage, Long> {

    List<UserImage> findAllByUserId(Long userId);

    List<UserImage> findAllByUserIdIn(List<Long> userIds);

    void deleteAllByUserId(Long userId);
}
