package com.sistemasweb.back_end_barber.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("scheduler")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Scheduler extends User {
    // Scheduler-specific fields can be added here in the future
}

