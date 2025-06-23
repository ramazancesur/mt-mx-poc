package com.mtmx.web.controller;

import com.mtmx.domain.entity.SwiftMessage;
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
        // Basic validation
        if (swiftMessageDto == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Geçersiz mesaj verisi"));
        }

        SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);
        if (result == null || result.getId() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Mesaj kaydedilemedi"));
        }

        return ResponseEntity.created(new URI("/api/swift-messages/" + result.getId()))
                .body(StandardResponse.success(result, "Mesaj başarıyla oluşturuldu"));
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
}
