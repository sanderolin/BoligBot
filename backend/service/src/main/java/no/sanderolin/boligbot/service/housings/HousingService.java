package no.sanderolin.boligbot.service.housings;

import jakarta.persistence.criteria.Expression;
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

import java.math.BigDecimal;

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
                eqIgnoreCase("rentalObjectId", criteria.rentalObjectId()),
                eqIgnoreCase("address", criteria.address()),
                eqIgnoreCase("name", criteria.name()),
                eqIgnoreCase("housingType", criteria.housingType()),
                eqIgnoreCase("city", criteria.city()),
                eqIgnoreCase("district", criteria.district()),
                rangeComparable("pricePerMonth", criteria.minPricePerMonthOrNull(), criteria.maxPricePerMonthOrNull(), Integer.class),
                rangeComparable("areaSqm", criteria.minAreaOrNull(), criteria.maxAreaOrNull(), BigDecimal.class)
        );
        return housingRepository.findAll(spec, pageable);
    }

    public HousingModel getHousingByRentalObjectId(String id) throws IllegalArgumentException {
        return housingRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Housing with id " + id + " not found", HousingModel.class));
    }

    private Specification<HousingModel> eqIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim().toLowerCase();
        return (root, q, cb)
                -> cb.equal(cb.lower(root.get(field)), v);
    }

    private <T extends Comparable<? super T>> Specification<HousingModel> rangeComparable(
            String field, T min, T max, Class<T> type) {

        if (min == null && max == null) return null;

        return (root, q, cb) -> {
            Expression<T> path = root.get(field).as(type);
            if (min != null && max != null) return cb.between(path, min, max);
            if (min != null) return cb.greaterThanOrEqualTo(path, min);
            return cb.lessThanOrEqualTo(path, max);
        };
    }

}
