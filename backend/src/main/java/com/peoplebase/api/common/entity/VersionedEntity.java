package com.peoplebase.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;

/** Optimistic concurrency for mutable aggregates and their editable details. */
@Getter
@MappedSuperclass
public abstract class VersionedEntity extends BaseEntity {
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
