package com.mtmx.web.controller;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.service.ConversionService;
import com.mtmx.service.SwiftMessageService;
import com.mtmx.web.dto.SwiftMessageDto;
import com.mtmx.web.dto.StandardResponse;
import com.mtmx.web.exception.ErrorDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Swift Message API", description = "API for managing SWIFT messages")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
        "http://localhost:5176" })
public class SwiftMessageController {

    private final SwiftMessageService swiftMessageService;
    private final ConversionService conversionService;
    private static final Logger log = LoggerFactory.getLogger(SwiftMessageController.class);

    @Operation(summary = "Get all messages with pagination", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping("/api/swift-messages")
    public ResponseEntity<StandardResponse<Page<SwiftMessageDto>>> getAllMessages(Pageable pageable) {
        Page<SwiftMessageDto> page = swiftMessageService.findAll(pageable);
        return ResponseEntity.ok().body(StandardResponse.success(page, "Mesajlar başarıyla getirildi"));
    }

    @Operation(summary = "Get messages by type with pagination", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping("/api/swift-messages/type/{messageType}")
    public ResponseEntity<StandardResponse<Page<SwiftMessageDto>>> getMessagesByType(@PathVariable String messageType,
            Pageable pageable) {
        Page<SwiftMessageDto> page = swiftMessageService.findByMessageType(messageType, pageable);
        return ResponseEntity.ok().body(StandardResponse.success(page, messageType + " mesajları başarıyla getirildi"));
    }

    @Operation(summary = "Get a message by its ID with automatic conversion", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping("/api/swift-messages/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable Long id) {
        try {
            log.info("Fetching SWIFT message with ID: {}", id);

            Optional<SwiftMessageDto> messageOpt = swiftMessageService.findOne(id);
            if (messageOpt.isEmpty()) {
                log.warn("SWIFT message not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(StandardResponse.error("Mesaj bulunamadı"));
            }

            SwiftMessageDto message = messageOpt.get();

            // Otomatik dönüşüm yap - eğer MX mesajı yoksa oluştur
            if (message.getGeneratedMxMessage() == null || message.getGeneratedMxMessage().isEmpty()) {
                try {
                    log.info("Converting MT message to MX for message ID: {}", id);
                    SwiftMessageDto convertedMessage = swiftMessageService.convertMtToMx(id);
                    log.info("Successfully converted and saved MX message for ID: {}", id);

                    return ResponseEntity.ok(StandardResponse.success(convertedMessage,
                            "Mesaj başarıyla getirildi ve MX formatına dönüştürüldü"));
                } catch (Exception e) {
                    log.error("Failed to convert MT to MX for message ID: {}", id, e);
                    // Dönüşüm başarısız olsa bile mesajı döndür
                    return ResponseEntity.ok(
                            StandardResponse.success(message, "Mesaj başarıyla getirildi (MX dönüştürme başarısız)"));
                }
            }

            log.info("SWIFT message found and returned with ID: {}", id);
            return ResponseEntity.ok(StandardResponse.success(message, "Mesaj başarıyla getirildi"));

        } catch (Exception e) {
            log.error("Error fetching SWIFT message with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Mesaj getirilirken hata oluştu: " + e.getMessage()));
        }
    }

    @Operation(summary = "Create a new message", responses = {
            @ApiResponse(responseCode = "201", description = "Oluşturuldu", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz veri", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/swift-messages")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> createMessage(@RequestBody SwiftMessageDto swiftMessageDto)
            throws URISyntaxException {
        try {
            SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(StandardResponse.success(result, "Mesaj başarıyla oluşturuldu"));
        } catch (Exception e) {
            log.error("Error creating SWIFT message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(StandardResponse.error("Mesaj oluşturulamadı: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update an existing message", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz veri", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PutMapping("/api/swift-messages/{id}")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> updateMessage(@PathVariable Long id,
            @RequestBody SwiftMessageDto swiftMessageDto) {
        swiftMessageDto.setId(id);
        SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);
        return ResponseEntity.ok().body(StandardResponse.success(result, "Mesaj başarıyla güncellendi"));
    }

    @Operation(summary = "Delete a message by its ID", responses = {
            @ApiResponse(responseCode = "204", description = "Silindi"),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @DeleteMapping("/api/swift-messages/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteMessage(@PathVariable Long id) {
        swiftMessageService.delete(id);
        return ResponseEntity.ok().body(StandardResponse.success(null, "Mesaj başarıyla silindi"));
    }

    @Operation(summary = "Convert MT message to MX format", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/swift-messages/{id}/convert")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> convertMtToMx(@PathVariable Long id,
            @RequestBody(required = false) String newMtMessage) {
        SwiftMessageDto result = swiftMessageService.convertMtToMx(id, newMtMessage);
        return ResponseEntity.ok().body(StandardResponse.success(result, "Mesaj başarıyla dönüştürüldü"));
    }

    @Operation(summary = "Convert MX message to MT format", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/swift-messages/{id}/convert-mx-to-mt")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> convertMxToMt(@PathVariable Long id) {
        try {
            SwiftMessageDto convertedMessage = swiftMessageService.convertMxToMt(id);
            return ResponseEntity
                    .ok(StandardResponse.success(convertedMessage, "MX message successfully converted to MT format"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(StandardResponse.error("Message not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Error converting MX to MT: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update MX XML content", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz XML", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PutMapping("/api/swift-messages/{id}/update-xml")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> updateXmlContent(@PathVariable Long id,
            @RequestBody String xmlContent) {
        try {
            SwiftMessageDto result = swiftMessageService.updateXmlContent(id, xmlContent);
            return ResponseEntity.ok().body(StandardResponse.success(result, "XML içeriği başarıyla güncellendi"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(StandardResponse.error("Mesaj bulunamadı: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(StandardResponse.error("Geçersiz XML: " + e.getMessage()));
        }
    }

    @Operation(summary = "Upload SWIFT message file", responses = {
            @ApiResponse(responseCode = "201", description = "Dosya başarıyla yüklendi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SwiftMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz dosya", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/swift-messages/upload")
    public ResponseEntity<StandardResponse<SwiftMessageDto>> uploadMessageFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "messageType", required = false) String messageType) {

        try {
            log.info("Uploading SWIFT message file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

            // File validation
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Dosya boş olamaz"));
            }

            if (file.getSize() > 1024 * 1024) { // 1MB limit
                return ResponseEntity.badRequest().body(StandardResponse.error("Dosya boyutu 1MB'dan büyük olamaz"));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt")) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Sadece .txt dosyaları kabul edilir"));
            }

            // Read file content
            String fileContent = new String(file.getBytes(), "UTF-8");
            if (fileContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(StandardResponse.error("Dosya içeriği boş olamaz"));
            }

            // Create message DTO
            SwiftMessageDto messageDto = new SwiftMessageDto();
            messageDto.setRawMtMessage(fileContent);

            // Auto-detect message type if not provided
            if (messageType == null || messageType.trim().isEmpty()) {
                messageType = detectMessageType(fileContent);
                log.info("Auto-detected message type: {}", messageType);
            }

            messageDto.setMessageType(MessageType.valueOf(messageType));

            // Extract basic information from MT message
            extractBasicInfoFromMt(fileContent, messageDto);

            // Save message
            SwiftMessageDto savedMessage = swiftMessageService.save(messageDto);

            log.info("Successfully uploaded and saved message with ID: {}", savedMessage.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(StandardResponse.success(savedMessage, "Dosya başarıyla yüklendi ve mesaj oluşturuldu"));

        } catch (Exception e) {
            log.error("Error uploading SWIFT message file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Dosya yüklenirken hata oluştu: " + e.getMessage()));
        }
    }

    private String detectMessageType(String mtContent) {
        if (mtContent.contains("{2:I102"))
            return "MT102";
        if (mtContent.contains("{2:I103"))
            return "MT103";
        if (mtContent.contains("{2:I202"))
            return "MT202";
        if (mtContent.contains("{2:I203"))
            return "MT203";
        return "MT103"; // Default
    }

    private void extractBasicInfoFromMt(String mtContent, SwiftMessageDto messageDto) {
        try {
            // Extract sender BIC from field 1
            if (mtContent.contains("{1:F01")) {
                int start = mtContent.indexOf("{1:F01") + 6;
                int end = mtContent.indexOf("}", start);
                if (end > start) {
                    String senderBic = mtContent.substring(start, end).substring(0, 11);
                    messageDto.setSenderBic(senderBic);
                }
            }

            // Extract receiver BIC from field 2
            if (mtContent.contains("{2:I")) {
                int start = mtContent.indexOf("{2:I") + 4;
                int end = mtContent.indexOf("}", start);
                if (end > start) {
                    String receiverBic = mtContent.substring(start, end).substring(0, 11);
                    messageDto.setReceiverBic(receiverBic);
                }
            }

            // Extract amount and currency from field 32A
            if (mtContent.contains(":32A:")) {
                int start = mtContent.indexOf(":32A:") + 5;
                int end = mtContent.indexOf("\n", start);
                if (end > start) {
                    String field32A = mtContent.substring(start, end).trim();
                    // Format: YYMMDDCURRENCYAMOUNT
                    if (field32A.length() >= 13) {
                        String currency = field32A.substring(6, 9);
                        String amountStr = field32A.substring(9).replace(",", ".");
                        try {
                            BigDecimal amount = new BigDecimal(amountStr);
                            messageDto.setCurrency(currency);
                            messageDto.setAmount(amount);
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse amount from field 32A: {}", field32A);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error extracting basic info from MT message", e);
        }
    }

    @Operation(summary = "Convert MT message to MX format using new JAXB-based converter", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Geçersiz mesaj", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/convert/mt-to-mx")
    public ResponseEntity<StandardResponse<String>> convertMtToMxNew(@RequestBody String mtMessage) {
        try {
            String mxMessage = conversionService.convertMtToMx(mtMessage);
            return ResponseEntity.ok()
                    .body(StandardResponse.success(mxMessage, "MT mesajı başarıyla MX formatına dönüştürüldü"));
        } catch (Exception e) {
            log.error("Error converting MT to MX: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(StandardResponse.error("Dönüşüm hatası: " + e.getMessage()));
        }
    }

    @Operation(summary = "Convert MX message to MT format using new JAXB-based converter", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Geçersiz mesaj", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu Hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/convert/mx-to-mt")
    public ResponseEntity<StandardResponse<String>> convertMxToMtNew(@RequestBody String mxMessage) {
        try {
            String mtMessage = conversionService.convertMxToMt(mxMessage);
            return ResponseEntity.ok()
                    .body(StandardResponse.success(mtMessage, "MX mesajı başarıyla MT formatına dönüştürüldü"));
        } catch (Exception e) {
            log.error("Error converting MX to MT: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(StandardResponse.error("Dönüşüm hatası: " + e.getMessage()));
        }
    }

    @Operation(summary = "Validate MT message", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Geçersiz mesaj", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/validate/mt")
    public ResponseEntity<StandardResponse<Boolean>> validateMtMessage(@RequestBody String mtMessage) {
        boolean isValid = conversionService.isValidMtMessage(mtMessage);
        String messageType = conversionService.getMtMessageType(mtMessage);

        if (isValid) {
            return ResponseEntity.ok().body(StandardResponse.success(true,
                    "MT mesajı geçerli. Mesaj tipi: " + messageType));
        } else {
            return ResponseEntity.badRequest().body(StandardResponse.error("MT mesajı geçersiz"));
        }
    }

    @Operation(summary = "Validate MX message", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Geçersiz mesaj", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/api/validate/mx")
    public ResponseEntity<StandardResponse<Boolean>> validateMxMessage(@RequestBody String mxMessage) {
        boolean isValid = conversionService.isValidMxMessage(mxMessage);
        String messageType = conversionService.getMxMessageType(mxMessage);

        if (isValid) {
            return ResponseEntity.ok().body(StandardResponse.success(true,
                    "MX mesajı geçerli. Mesaj tipi: " + messageType));
        } else {
            return ResponseEntity.badRequest().body(StandardResponse.error("MX mesajı geçersiz"));
        }
    }

    @Operation(summary = "Get supported message types", responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/api/supported-types")
    public ResponseEntity<StandardResponse<Object>> getSupportedTypes() {
        return ResponseEntity.ok().body(StandardResponse.success(Map.of(
                "mtTypes", conversionService.getSupportedMtMessageTypes(),
                "mxTypes", conversionService.getSupportedMxMessageTypes()),
                "Desteklenen mesaj tipleri başarıyla getirildi"));
    }
}
