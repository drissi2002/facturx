package com.wevioo.facturx.ressource;

import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/facturx")
public class FacturxController {
    private static final Logger logger = LoggerFactory.getLogger(FacturxController.class);

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public String extractFile(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ZUGFeRDImporter importer = new ZUGFeRDImporter(inputStream);
            return "<response>" + importer.getUTF8() + "</response>";
        } catch (IOException e) {
            logger.error("Error extracting XML", e);
            return "<response>Error extracting XML</response>";
        }
    }

    @PostMapping(value = "/combine", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> combineFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("xml") String xml,
            @RequestParam("format") String format,
            @RequestParam("version") int version) {
        try (InputStream pdfInputStream = file.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                    .setProducer("Wevioo API")
                    .setCreator(System.getProperty("user.name"))
                    .setZUGFeRDVersion(version)
                    .load(pdfInputStream);

            if ("fx".equalsIgnoreCase(format)) {
                exporter.setFacturX();
            }

            exporter.setXML(xml.getBytes("UTF-8"));
            exporter.export(output);

            byte[] bytes = output.toByteArray();

            // Generate a unique file name using timestamp
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String uniqueFileName = "invoice_" + timestamp + ".pdf";

            // Save the PDF to src/main/resources/templates with the unique name
            File pdfFile = new File("src/main/resources/templates/" + uniqueFileName);
            FileCopyUtils.copy(output.toByteArray(), pdfFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + uniqueFileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);
        } catch (IOException e) {
            logger.error("Error combining PDF and XML", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

