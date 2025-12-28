package no.sanderolin.boligbot.web.v1.housing.converters;

import no.sanderolin.boligbot.service.housing.SortDirection;
import no.sanderolin.boligbot.web.v1.common.exception.BadRequestException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Injected by Spring and used to convert query parameters to SortDirection enum values.
 */
@Component
public class SortDirectionConverter implements Converter<String, SortDirection> {

    @Override
    public SortDirection convert(String source) {
        try {
            return SortDirection.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid sort direction: " + source);
        }
    }
}
