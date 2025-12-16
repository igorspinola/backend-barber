package com.sistemasweb.back_end_barber.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAppointmentRequest {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;
}

