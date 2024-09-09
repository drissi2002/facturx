package com.wevioo.facturx.ressource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wevioo.facturx.domain.entity.InvoiceData;
import com.wevioo.facturx.service.InvoiceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper for JSON parsing


    @PostMapping("/generate")
    public ResponseEntity<?> generateInvoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format,
            @RequestParam("invoiceData") String invoiceDataString) {
        try {
            // Parse the JSON string into an InvoiceData object
            InvoiceData invoiceData = objectMapper.readValue(invoiceDataString, InvoiceData.class);

            // Generate the PDF and save it
            invoiceService.generateInvoicePdfA3(invoiceData, file, format);

            // Load the saved PDF
            File pdfFile = new File("src/main/resources/templates/generatedInvoice.pdf");
            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generatedInvoice.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
