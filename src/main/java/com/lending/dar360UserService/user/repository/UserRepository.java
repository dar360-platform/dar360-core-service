package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmailIgnoreCaseAndStatusNot(String email , Integer status);

  @Query(value = "SELECT u FROM User u WHERE u.fullName LIKE %:fullName% or u.email LIKE %:email%")
  Page<User> findByFullNameOrEmail(@Param("fullName") String fullName,
                                   @Param("email") String email, Pageable pageable);

  Optional<User> findByEmailIgnoreCase(String email);
  Optional<User>  findByEmployeeIdIgnoreCase(String employeeId);
  Optional<User> findFirstByOrderByCreatedDateDesc();

  @Query(
      value =
          "select u.email from User u WHERE u.status =1  AND (lower(u.approvalLevel)) like lower(concat('%', concat(:level, '%')))")
  List<String> findEmailsByApprovalLevelLikeIgnoreCase(String level);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
  boolean existsByEmployeeIdIgnoreCaseAndIdNot(String employeeId, UUID id);
  List<User> findAllByStatusIsNotOrderByModifiedDateDesc(Integer status);

  @Query(value =
          "select u.email from User u inner join u.department d where u.status = 1 and lower(d.name) like lower(concat('%', :department, '%'))")
  List<String> findEmailsByDepartmentLikeIgnoreCase(String department);

  List<User> findAllByStatus(int value);

  @Query(
          value =
                  "select u from User u WHERE u.status =1  AND (lower(u.approvalLevel)) like lower(concat('%', concat(:level, '%')))")
  List<User> findUserByApprovalLevelLikeIgnoreCase(String level);

  List<User> findAllByCodeIn(List<String> codes);
}
