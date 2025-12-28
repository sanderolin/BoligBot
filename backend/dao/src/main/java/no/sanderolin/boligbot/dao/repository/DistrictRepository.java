package no.sanderolin.boligbot.dao.repository;

import no.sanderolin.boligbot.dao.model.DistrictModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DistrictRepository extends JpaRepository<DistrictModel, Long> {
    List<DistrictModel> findAllByCityIdIn(Collection<Long> cityIds);
}
