package com.peoplebase.api.recruitment.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Hồ sơ ứng viên, độc lập với Employee. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "candidates")
public class Candidate extends VersionedEntity {

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "phone", nullable = true, length = 255)
    private String phone;

    @Column(name = "date_of_birth", nullable = true)
    private LocalDate dateOfBirth;

    @Column(name = "source", nullable = true, length = 255)
    private String source;

    @Column(name = "cv_reference", nullable = true, length = 255)
    private String cvReference;

    @Column(name = "note", nullable = true, length = 2000)
    private String note;
}
