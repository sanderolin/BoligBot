package no.sanderolin.boligbot.web.v1.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
    List<T> items,
    int page,
    int size,
    long total,
    boolean hasNext
) {
    public static <S, T> PagedResponse<T> of(Page<S> page, Function<S, T> mapper) {
        List<T> items = page.getContent().stream().map(mapper).toList();
        return new PagedResponse<>(items, page.getNumber(), page.getSize(), page.getTotalElements(), page.hasNext());
    }
}
