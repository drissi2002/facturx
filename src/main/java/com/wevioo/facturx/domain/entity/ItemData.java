package com.wevioo.facturx.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class ItemData {
    private String productName;
    private String productDescription;
    private String productUnitCode; // Must follow UN/ECE Recommendation 20
    private BigDecimal productVatRate; // Ensure it's a valid VAT rate
    private BigDecimal quantity;
    private BigDecimal unitPrice;
}