package com.mtmx.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * XSD tabanlı MX mesaj doğrulama servisi
 * MT-MX karşılıkları için XSD şemalarını kullanarak doğrulama yapar
 */
@Service
public class XsdValidationService {
    
    private static final Logger logger = Logger.getLogger(XsdValidationService.class.getName());
    
    // XSD dosya yolları - yeni indirilen XSD dosyaları
    private static final String PACS_004_XSD = "xsd/pacs.004.001.02.xsd"; // MT102
    private static final String PACS_008_XSD = "xsd/pacs.008.001.06.xsd"; // MT103
    private static final String PACS_009_XSD = "xsd/pacs.009.001.08.xsd"; // MT202, MT202COV, MT203
    
    /**
     * MT102 için pacs.004.001.02 XSD ile doğrulama
     */
    public ValidationResult validatePacs004(String mxXml) {
        return validateAgainstXsd(mxXml, PACS_004_XSD, "pacs.004.001.02");
    }
    
    /**
     * MT103 için pacs.008.001.06 XSD ile doğrulama
     */
    public ValidationResult validatePacs008(String mxXml) {
        return validateAgainstXsd(mxXml, PACS_008_XSD, "pacs.008.001.06");
    }
    
    /**
     * MT202/MT203/MT202COV için pacs.009.001.08 XSD ile doğrulama
     */
    public ValidationResult validatePacs009(String mxXml) {
        return validateAgainstXsd(mxXml, PACS_009_XSD, "pacs.009.001.08");
    }
    
    /**
     * MT tipine göre otomatik XSD doğrulaması
     */
    public ValidationResult validateByMtType(String mxXml, String mtType) {
        if (mtType == null) {
            return ValidationResult.invalid("MT tipi belirtilmedi");
        }
        
        switch (mtType.toUpperCase()) {
            case "MT102":
                return validatePacs004(mxXml);
            case "MT103":
                return validatePacs008(mxXml);
            case "MT202":
            case "MT203":
            case "MT202COV":
                return validatePacs009(mxXml);
            default:
                return ValidationResult.invalid("Desteklenmeyen MT tipi: " + mtType);
        }
    }
    
    /**
     * Belirtilen XSD dosyasına karşı XML doğrulaması
     */
    private ValidationResult validateAgainstXsd(String xmlContent, String xsdPath, String schemaType) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return ValidationResult.invalid("XML içeriği boş");
        }
        
        try {
            // XSD dosyasını yükle
            ClassPathResource xsdResource = new ClassPathResource(xsdPath);
            if (!xsdResource.exists()) {
                logger.warning("XSD dosyası bulunamadı: " + xsdPath);
                return ValidationResult.invalid("XSD şeması bulunamadı: " + xsdPath);
            }
            
            // Schema factory oluştur
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdResource.getURL());
            
            // Validator oluştur ve doğrula
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            
            logger.info("XML başarıyla doğrulandı: " + schemaType);
            return ValidationResult.valid("XML " + schemaType + " şemasına uygun");
            
        } catch (SAXException e) {
            String errorMsg = "XSD doğrulama hatası: " + e.getMessage();
            logger.warning(errorMsg);
            return ValidationResult.invalid(errorMsg);
        } catch (IOException e) {
            String errorMsg = "XSD dosyası okuma hatası: " + e.getMessage();
            logger.severe(errorMsg);
            return ValidationResult.invalid(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Beklenmeyen doğrulama hatası: " + e.getMessage();
            logger.severe(errorMsg);
            return ValidationResult.invalid(errorMsg);
        }
    }
    
    /**
     * Toplu doğrulama - birden fazla XML'i aynı anda doğrular
     */
    public List<ValidationResult> validateBatch(List<String> xmlList, String mtType) {
        List<ValidationResult> results = new ArrayList<>();
        
        for (int i = 0; i < xmlList.size(); i++) {
            String xml = xmlList.get(i);
            ValidationResult result = validateByMtType(xml, mtType);
            result.setIndex(i);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Doğrulama sonucu sınıfı
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private int index = -1;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult valid(String message) {
            return new ValidationResult(true, message);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
        
        // Getters and setters
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, message='%s', index=%d}", 
                    valid, message, index);
        }
    }
}
