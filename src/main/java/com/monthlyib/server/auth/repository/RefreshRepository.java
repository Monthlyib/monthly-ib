package com.monthlyib.server.auth.repository;

import com.monthlyib.server.auth.entity.RefreshEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends CrudRepository<RefreshEntity, String> {
}
