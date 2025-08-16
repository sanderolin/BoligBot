package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HousingRepository extends JpaRepository<HousingModel, String> {

    List<HousingModel> findAllByRentalObjectIdIn(Collection<String> rentalObjectIds);
}
