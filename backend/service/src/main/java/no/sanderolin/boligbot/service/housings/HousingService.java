package no.sanderolin.boligbot.service.housings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousingService {

    private final HousingRepository housingRepository;

    public Page<HousingModel> searchHousings(HousingSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(
                criteria.pageOrDefault(),
                criteria.sizeOrDefault(),
                criteria.toSpringSort()
        );

        Specification<HousingModel> spec = Specification.allOf(
                eqIgnoreCase("city", criteria.city()),
                eqIgnoreCase("district", criteria.district()),
                eqIgnoreCase("housingType", criteria.housingType())
        );
        return housingRepository.findAll(spec, pageable);
    }

    public HousingModel getHousingById(String id) throws IllegalArgumentException {
        return housingRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Housing with id " + id + " not found", HousingModel.class));
    }

    private Specification<HousingModel> eqIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim().toLowerCase();
        return (root, q, cb)
                -> cb.equal(cb.lower(root.get(field)), v);
    }
}
