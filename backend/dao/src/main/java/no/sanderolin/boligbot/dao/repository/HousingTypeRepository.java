package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HousingTypeRepository extends JpaRepository<HousingTypeModel, Long> {
    List<HousingTypeModel> findAllByNameIn(Collection<String> names);
}
