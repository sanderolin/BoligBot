package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.CityModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CityRepository extends JpaRepository<CityModel, Long> {
    List<CityModel> findAllByNameIn(Collection<String> names);
}
