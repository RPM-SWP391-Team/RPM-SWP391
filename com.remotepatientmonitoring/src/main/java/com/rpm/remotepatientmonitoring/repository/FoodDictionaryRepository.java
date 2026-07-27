package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface FoodDictionaryRepository extends JpaRepository<FoodDictionary, Integer> {
    List<FoodDictionary> findByIsActiveTrue();
    Page<FoodDictionary> findByIsActiveTrue(Pageable pageable);
    Page<FoodDictionary> findByIsActiveTrueAndFoodNameContainingIgnoreCaseOrIsActiveTrueAndEnglishNameContainingIgnoreCase(String foodName, String englishName, Pageable pageable);
    boolean existsByFoodCode(String foodCode);

    @Query("SELECT f FROM FoodDictionary f WHERE " +
           "(:isActive IS NULL OR f.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(f.foodName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(f.englishName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<FoodDictionary> searchFoodsWithStatus(
            @Param("search") String search, 
            @Param("isActive") Boolean isActive, 
            Pageable pageable
    );
}
