package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HousingRepository extends JpaRepository<HousingModel, String> {

}
