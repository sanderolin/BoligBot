package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HousingRepository extends JpaRepository<HousingModel, String> {

    @Query("""
        select h
        from HousingModel h
        order by h.rentalObjectId asc
    """)
    Iterable<HousingModel> findAllOrderByRentalObjectIdAsc();
}
