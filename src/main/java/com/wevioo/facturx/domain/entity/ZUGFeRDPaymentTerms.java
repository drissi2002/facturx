package com.wevioo.facturx.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.mustangproject.ZUGFeRD.IZUGFeRDPaymentDiscountTerms;
import org.mustangproject.ZUGFeRD.IZUGFeRDPaymentTerms;

import java.util.Date;

@Getter
@Setter
public class ZUGFeRDPaymentTerms implements IZUGFeRDPaymentTerms {
    private Date paymentDueDate;
    private String paymentTermsText;

    public void setPaymentDueDate(Date paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }


    public void setPaymentTermsText(String paymentTermsText) {
        this.paymentTermsText = paymentTermsText;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Date getDueDate() {
        return null;
    }

    @Override
    public IZUGFeRDPaymentDiscountTerms getDiscountTerms() {
        return null;
    }
}
