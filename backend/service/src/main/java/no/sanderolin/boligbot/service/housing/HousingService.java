package no.sanderolin.boligbot.service.housing;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
                containsIgnoreCase("rentalObjectId", criteria.rentalObjectId()),
                containsIgnoreCase("address", criteria.address()),
                containsIgnoreCase("name", criteria.name()),
                containsIgnoreCaseJoin(criteria.housingType(), "housingType", "name"),
                containsIgnoreCaseJoin(criteria.district(), "district", "name"),
                containsIgnoreCaseJoin(criteria.city(), "district", "city", "name"),
                rangeComparable("pricePerMonth", criteria.minPricePerMonthOrNull(), criteria.maxPricePerMonthOrNull(), Integer.class),
                rangeComparable("areaSqm", criteria.minAreaOrNull(), criteria.maxAreaOrNull(), BigDecimal.class)
        );
        return housingRepository.findAll(spec, pageable);
    }

    public HousingModel getHousingByRentalObjectId(String id) throws ObjectNotFoundException {
        return housingRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Housing with id " + id + " not found", HousingModel.class));
    }

    private Specification<HousingModel> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        String v = "%" + value.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get(field)), v);
    }

    private Specification<HousingModel> containsIgnoreCaseJoin(String value, String... path) {
        if (value == null || value.isBlank()) return null;
        String v = "%" + value.trim().toLowerCase() + "%";

        return (root, q, cb) -> {
            From<?, ?> from = root; // Root implements From
            for (int i = 0; i < path.length - 1; i++) {
                from = from.join(path[i]);
            }
            String last = path[path.length - 1];
            Path<String> p = from.get(last);
            return cb.like(cb.lower(p), v);
        };
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
