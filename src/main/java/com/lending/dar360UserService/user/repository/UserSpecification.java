package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.dto.SearchUserForm;
import com.lending.dar360UserService.user.enums.UserStatus;
import com.lending.dar360UserService.user.model.User;
import com.lending.dar360UserService.user.util.DateTimeUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserSpecification implements Specification<User> {

  private static final long serialVersionUID = 1L;

  private transient SearchUserForm searchUserForm;

  public UserSpecification(SearchUserForm searchUserForm) {
    this.searchUserForm = searchUserForm;
  }

  @Override
  public Predicate toPredicate(
          Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    List<Predicate> predicates = new ArrayList<>();
    Set<UUID> ids = this.searchUserForm.getIds();
    String fromDate = this.searchUserForm.getFromDate();
    String toDate = this.searchUserForm.getToDate();
    String keyword = StringUtils.toRootLowerCase(this.searchUserForm.getKeyword());
    if (CollectionUtils.isNotEmpty(ids)) {
      predicates.add(criteriaBuilder.in(root.get("id")).value(ids));
    }

    if (StringUtils.isNotEmpty(keyword)) {
      predicates.add(
          criteriaBuilder.or(
              criteriaBuilder.like(
                  criteriaBuilder.lower(root.get("code")), "%" + keyword.trim() + "%"),
              criteriaBuilder.like(
                  criteriaBuilder.lower(root.get("employeeId")), "%" + keyword.trim() + "%"),
              criteriaBuilder.like(
                  criteriaBuilder.lower(root.get("fullName")), "%" + keyword.trim() + "%"),
              criteriaBuilder.like(
                  criteriaBuilder.lower(root.get("email")), "%" + keyword.trim() + "%")));
    }
    if (StringUtils.isNotEmpty(fromDate)) {
      predicates.add(
          criteriaBuilder.greaterThanOrEqualTo(
              root.get("modifiedDate"), DateTimeUtils.convertToOffsetDateTime(fromDate,"fromDate")));
    }
    if (StringUtils.isNotEmpty(toDate)) {
      predicates.add(
          criteriaBuilder.lessThanOrEqualTo(
              root.get("modifiedDate"), DateTimeUtils.convertToOffsetDateTime(toDate,"toDate")));
    }

    if(searchUserForm.getStatus() != null){
      predicates.add(
              criteriaBuilder.equal(
                      root.get("status"), searchUserForm.getStatus()));
    }else{
      predicates.add(
              criteriaBuilder.notEqual(
                      root.get("status"), UserStatus.DELETE.getValue()));
    }
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
