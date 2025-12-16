package com.sistemasweb.back_end_barber.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "HAIR|BEARD|COMBO|OTHER|hair|beard|combo|other",
             message = "Category must be one of: hair, beard, combo, other")
    private String category;

    private Boolean active = true;

    private Long barbershopId;

    private String imageUrl;
}

