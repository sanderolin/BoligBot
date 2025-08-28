package no.sanderolin.boligbot.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
public abstract class ImportableEntity extends BaseEntity {

    @Column(name = "last_imported_at", nullable = false)
    private Instant lastImportedAt;
}
