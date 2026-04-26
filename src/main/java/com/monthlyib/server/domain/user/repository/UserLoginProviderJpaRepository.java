package com.monthlyib.server.domain.user.repository;

import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.domain.user.entity.UserLoginProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLoginProviderJpaRepository extends JpaRepository<UserLoginProvider, Long> {

    Optional<UserLoginProvider> findByProviderAndProviderEmail(LoginType provider, String providerEmail);

    List<UserLoginProvider> findAllByUserUserId(Long userId);

    List<UserLoginProvider> findAllByUserUserIdIn(List<Long> userIds);
}
