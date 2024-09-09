package com.wevioo.facturx.service;


import com.wevioo.facturx.domain.entity.InvoiceData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface InvoiceService {
    /**
     * Generates a Factur-X PDF A3 invoice based on the provided invoice data.
     *
     * @param invoiceData The invoice data to include in the PDF.
     * @throws IOException If an error occurs during PDF generation.
     */
    void generateInvoicePdfA3(InvoiceData invoiceData, MultipartFile file, String format) throws IOException;
}