package no.sanderolin.backend.repository;

import no.sanderolin.backend.model.HousingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HousingRepository extends JpaRepository<HousingModel, Long> {

}
