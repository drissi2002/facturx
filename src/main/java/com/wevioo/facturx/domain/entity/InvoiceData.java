package com.wevioo.facturx.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InvoiceData {
    private Date issueDate;
    private Date dueDate;
    private Date deliveryDate;
    private String senderName;
    private String senderStreet;
    private String senderPostalCode;
    private String senderCity;
    private String senderCountry;
    private String senderTaxID;
    private String senderVATID;
    private String senderBankAccount;
    private String senderBankIBAN;
    private String recipientName;
    private String recipientStreet;
    private String recipientPostalCode;
    private String recipientCity;
    private String recipientCountry;
    private String recipientContactName;
    private String recipientContactPhone;
    private String recipientContactEmail;
    private String invoiceNumber;
    private String referenceNumber; // For preceding invoice reference
    private String correction; // Indicating if this is a correction invoice
    private String precedingInvoiceReference;
    private List<ItemData> items;
    private ZUGFeRDPaymentTerms paymentTerms; // Ensure this is present if amount due is positive
}
