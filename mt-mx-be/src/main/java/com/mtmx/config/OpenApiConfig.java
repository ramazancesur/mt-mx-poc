package com.mtmx.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for MT-MX SWIFT Message Conversion System
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🏦 MT-MX SWIFT Message Conversion API")
                        .description("**Modern SWIFT Mesaj Dönüştürme Sistemi**\n\n" +
                                "Bu API, SWIFT MT formatındaki mesajları ISO 20022 MX formatına dönüştürmek için tasarlanmış kapsamlı bir sistemdir.\n\n"
                                +
                                "## 🎯 Temel Özellikler\n\n" +
                                "### 📊 Desteklenen Mesaj Tipleri\n" +
                                "- **MT102**: Multiple Customer Credit Transfer\n" +
                                "- **MT103**: Single Customer Credit Transfer\n" +
                                "- **MT202**: General Financial Institution Transfer\n" +
                                "- **MT202COV**: Cover Payment\n" +
                                "- **MT203**: Multiple General Financial Institution Transfer\n\n" +
                                "### 🔄 Dönüştürme İşlemleri\n" +
                                "- **MT → MX**: Geleneksel SWIFT mesajlarını ISO 20022 XML formatına dönüştürme\n" +
                                "- **MX → MT**: XML mesajlarını geleneksel MT formatına geri dönüştürme\n" +
                                "- **XML Güncelleme**: Canlı XML düzenleme ve otomatik MT format güncelleme\n\n" +
                                "### 🛠️ CRUD Operasyonları\n" +
                                "- Mesaj oluşturma, okuma, güncelleme ve silme\n" +
                                "- Sayfalanmış veri görüntüleme\n" +
                                "- Tip bazında filtreleme\n" +
                                "- Otomatik format dönüştürme\n\n" +
                                "## 🚀 Kullanım Örnekleri\n\n" +
                                "### Mesajları Listeleme\n" +
                                "```\n" +
                                "GET /api/swift-messages?page=0&size=10\n" +
                                "GET /api/swift-messages/type/MT103?page=0&size=10\n" +
                                "```\n\n" +
                                "### Mesaj Dönüştürme\n" +
                                "```\n" +
                                "POST /api/swift-messages/{id}/convert\n" +
                                "POST /api/swift-messages/{id}/convert-mx-to-mt\n" +
                                "```\n\n" +
                                "### XML Güncelleme\n" +
                                "```\n" +
                                "PUT /api/swift-messages/{id}/update-xml\n" +
                                "Content-Type: application/json\n" +
                                "Body: \"<?xml version='1.0'?>...</xml>\"\n" +
                                "```\n\n" +
                                "## 📈 Response Format\n\n" +
                                "Tüm API yanıtları standart format kullanır:\n" +
                                "```json\n" +
                                "{\n" +
                                "  \"success\": true,\n" +
                                "  \"message\": \"İşlem başarılı\",\n" +
                                "  \"data\": { ... },\n" +
                                "  \"timestamp\": \"2025-06-24T10:15:30\"\n" +
                                "}\n" +
                                "```\n\n" +
                                "## 🔍 Hata Yönetimi\n\n" +
                                "- **400 Bad Request**: Geçersiz parametre veya veri\n" +
                                "- **404 Not Found**: Kaynak bulunamadı\n" +
                                "- **500 Internal Server Error**: Sunucu hatası\n\n" +
                                "## 🌐 Frontend Integration\n\n" +
                                "Bu API, React frontend uygulaması ile entegre çalışır:\n" +
                                "- **Frontend URL**: http://localhost:3000\n" +
                                "- **D3.js Visualization**: İnteraktif XML ağaç görselleştirmesi\n" +
                                "- **Real-time Updates**: Canlı veri güncelleme\n\n" +
                                "---\n\n" +
                                "**💡 İpucu**: Bu API'yi test etmek için Swagger UI'ın \"Try it out\" özelliğini kullanabilirsiniz.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MT-MX Development Team")
                                .email("dev-team@mtmx.com")
                                .url("https://github.com/your-org/mt-mx-poc"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8082")
                                .description("Development Server (Alternative)"),
                        new Server()
                                .url("https://api.mtmx.com")
                                .description("Production Server (Future)")));
    }
}