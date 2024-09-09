package com.wevioo.facturx.service.impl;

import com.wevioo.facturx.domain.entity.InvoiceData;
import com.wevioo.facturx.domain.entity.ItemData;
import com.wevioo.facturx.domain.entity.ZUGFeRDPaymentTerms;
import com.wevioo.facturx.service.InvoiceService;
import org.mustangproject.BankDetails;
import org.mustangproject.Contact;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.IZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.Profile;
import org.mustangproject.ZUGFeRD.Profiles;
import org.mustangproject.ZUGFeRD.TransactionCalculator;
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Override
    public void generateInvoicePdfA3( InvoiceData invoiceData, MultipartFile file, String format) throws IOException {
        // Create and configure the ZUGFeRDExporter
        InputStream pdfInputStream = file.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .setProducer("Wevioo API")
                .setCreator(System.getProperty("user.name"))
                .load(pdfInputStream);

        if ("fx".equalsIgnoreCase(format)) {
            exporter.setFacturX();
        }

        // Create the invoice object and set its properties
        Invoice invoice = createInvoice(invoiceData);

        // Export the invoice to a PDF file
        generateAndSavePdf(exporter, invoice, output);

        // Optionally verify the invoice
        TransactionCalculator calculator = new TransactionCalculator(invoice);
        System.out.println("Total Grand Amount: " + calculator.getGrandTotal());

        // Get the FacturX XML as a string
        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.generateXML(invoice);
        String facturXXML = new String(zf2p.getXML(), StandardCharsets.UTF_8);

        // Print the XML to the logs
        System.out.println("Valid FacturX XML: \n" + facturXXML);
    }

    private Invoice createInvoice(InvoiceData invoiceData) {
        Invoice invoice = new Invoice()
                .setIssueDate(invoiceData.getIssueDate())
                .setDueDate(invoiceData.getDueDate())
                .setDeliveryDate(invoiceData.getDeliveryDate())
                .setSender(new TradeParty(invoiceData.getSenderName(), invoiceData.getSenderStreet(),
                        invoiceData.getSenderPostalCode(), invoiceData.getSenderCity(),
                        invoiceData.getSenderCountry())
                        .addBankDetails(new BankDetails(invoiceData.getSenderBankAccount(), invoiceData.getSenderBankIBAN())))
                .setRecipient(new TradeParty(invoiceData.getRecipientName(), invoiceData.getRecipientStreet(),
                        invoiceData.getRecipientPostalCode(), invoiceData.getRecipientCity(),
                        invoiceData.getRecipientCountry())
                        .setContact(new Contact(invoiceData.getRecipientContactName(), invoiceData.getRecipientContactPhone(),
                                invoiceData.getRecipientContactEmail())))
                .setNumber(invoiceData.getInvoiceNumber())
                .setOwnTaxID(invoiceData.getSenderTaxID())
                .setOwnVATID(invoiceData.getSenderVATID())
                .setReferenceNumber(invoiceData.getReferenceNumber())
                .setCorrection(invoiceData.getCorrection() != null ? invoiceData.getCorrection() : null);

        invoiceData.getItems().forEach(itemData -> {
            BigDecimal lineTotal = itemData.getUnitPrice().multiply(itemData.getQuantity());
            BigDecimal vatAmount = lineTotal.multiply(itemData.getProductVatRate()).setScale(2, RoundingMode.HALF_UP);

            invoice.addItem(new Item(
                    new Product(itemData.getProductName(), itemData.getProductDescription(), itemData.getProductUnitCode(),
                            vatAmount),
                    itemData.getQuantity(),
                    itemData.getUnitPrice()
            ));
        });

        ZUGFeRDPaymentTerms paymentTerms = new ZUGFeRDPaymentTerms();
        paymentTerms.setPaymentDueDate(invoiceData.getPaymentTerms().getPaymentDueDate());
        paymentTerms.setPaymentTermsText(invoiceData.getPaymentTerms().getPaymentTermsText());
        invoice.setPaymentTerms(paymentTerms);

        return invoice;
    }

    private void generateAndSavePdf(ZUGFeRDExporterFromA3 exporter, Invoice invoice, ByteArrayOutputStream output) throws IOException {
        exporter.setTransaction(invoice);
        exporter.export(output);

        // Save the generated PDF to resources/templates
        File fileToSave = new File("src/main/resources/templates/generatedInvoice.pdf");
        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            fos.write(output.toByteArray());
        }
    }
}