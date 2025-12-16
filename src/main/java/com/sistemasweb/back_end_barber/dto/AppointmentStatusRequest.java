package com.sistemasweb.back_end_barber.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // confirmed, completed, cancelled
}

