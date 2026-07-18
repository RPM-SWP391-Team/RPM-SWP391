package com.rpm.remotepatientmonitoring.repository;

import com.rpm.remotepatientmonitoring.model.AppRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppRatingRepository extends JpaRepository<AppRating, Integer> {
    List<AppRating> findByAccountId(Integer accountId);

    @Query("SELECT COUNT(r) FROM AppRating r WHERE r.account.id = :accountId AND r.createdAt >= :since")
    long countByAccountIdAndCreatedAtAfter(@Param("accountId") Integer accountId, @Param("since") LocalDateTime since);

    Page<AppRating> findByRatingValueIn(List<Integer> ratingValues, Pageable pageable);
    Page<AppRating> findAll(Pageable pageable);
}
