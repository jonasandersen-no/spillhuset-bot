package com.bjoggis.spillhuset.repository.valorant;

import com.bjoggis.spillhuset.entity.valorant.ValorantPremierEntry;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ValorantPremierEntryRepository extends
    JpaRepository<ValorantPremierEntry, Long> {

  @Query("select p from ValorantPremierEntry p where date(p.date) = ?1")
  Optional<ValorantPremierEntry> findByDate(LocalDate date);


}
