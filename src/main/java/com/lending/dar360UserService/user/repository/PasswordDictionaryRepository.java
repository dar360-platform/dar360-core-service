package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.PasswordDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordDictionaryRepository extends JpaRepository<PasswordDictionary, Long> {

    @Query("select case when count(1) > 0 then true else false end from PasswordDictionary p where lower(:password) like '%' || lower(p.word) || '%'")
    boolean containDictionaryWord(String password);
}
