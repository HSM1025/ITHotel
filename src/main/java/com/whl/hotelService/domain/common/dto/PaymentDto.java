package com.whl.hotelService.domain.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private Long reservationId;
    private String userid;
    private String impUid;
    private String merchantUid;
    private String payMethod;
    private Long paidAmount;
    private String status;
    private String address;
    private String name;
    private LocalDateTime payDate;
}
