package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface HousingRepository extends JpaRepository<HousingModel, String>, JpaSpecificationExecutor<HousingModel> {

    List<HousingModel> findAllByRentalObjectIdIn(Collection<String> rentalObjectIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE HousingModel h
        SET h.isAvailable = true
        WHERE h.rentalObjectId IN :rentalObjectIds
          AND h.isAvailable <> true
        """
    )
    int markAvailableIfInIds(@Param("rentalObjectIds") List<String> rentalObjectIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE HousingModel h
        SET h.isAvailable = false,
        h.availableFromDate = null
        WHERE h.isAvailable = true
          AND h.rentalObjectId NOT IN :rentalObjectIds
        """
    )
    int markUnavailableIfNotInIds(@Param("rentalObjectIds") List<String> rentalObjectIds);

    @Modifying
    @Query(
        """
        UPDATE HousingModel h
        SET h.availableFromDate = :availableFromDate
        WHERE h.rentalObjectId = :rentalObjectId
          AND (
               h.availableFromDate IS NULL
            OR h.availableFromDate <> :availableFromDate
          )
        """
    )
    int updateAvailableFromDate(@Param("rentalObjectId") String rentalObjectId,
                                @Param("availableFromDate") LocalDate availableFromDate);

}
