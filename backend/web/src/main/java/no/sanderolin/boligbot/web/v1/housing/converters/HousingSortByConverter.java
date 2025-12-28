package no.sanderolin.boligbot.web.v1.housing.converters;

import no.sanderolin.boligbot.service.housing.HousingSortBy;
import no.sanderolin.boligbot.web.v1.common.exception.BadRequestException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Injected by Spring and used to convert query parameters to HousingSortBy enum values.
 */
@Component
public class HousingSortByConverter implements Converter<String, HousingSortBy> {

    @Override
    public HousingSortBy convert(String source) {
        String s = source.trim();
        if (s.isEmpty()) return null;

        for (HousingSortBy v : HousingSortBy.values()) {
            if (v.property().equalsIgnoreCase(s) || v.name().equalsIgnoreCase(s)) {
                return v;
            }
        }
        throw new BadRequestException("Invalid sortBy: " + source);
    }
}
