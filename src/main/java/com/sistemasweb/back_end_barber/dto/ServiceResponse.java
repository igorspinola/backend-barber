package com.sistemasweb.back_end_barber.dto;

import com.sistemasweb.back_end_barber.model.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String category;
    private Boolean active;
    private Long barbershopId;
    private String imageUrl;
}

