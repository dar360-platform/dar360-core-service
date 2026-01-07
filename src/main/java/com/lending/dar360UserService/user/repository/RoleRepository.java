package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findAllByNameInIgnoreCase(Collection<String> names);
}
