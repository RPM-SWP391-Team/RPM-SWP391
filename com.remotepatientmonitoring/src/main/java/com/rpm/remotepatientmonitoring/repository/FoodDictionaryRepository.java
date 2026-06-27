package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodDictionaryRepository extends JpaRepository<FoodDictionary, Integer> {
    List<FoodDictionary> findByIsActiveTrue();
}
