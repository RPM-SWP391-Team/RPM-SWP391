package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface FoodDictionaryRepository extends JpaRepository<FoodDictionary, Integer> {
    List<FoodDictionary> findByIsActiveTrue();
    Page<FoodDictionary> findByFoodNameContainingIgnoreCaseOrEnglishNameContainingIgnoreCase(String foodName, String englishName, Pageable pageable);
    boolean existsByFoodCode(String foodCode);
}
