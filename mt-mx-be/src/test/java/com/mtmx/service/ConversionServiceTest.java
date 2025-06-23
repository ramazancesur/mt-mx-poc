package com.mtmx.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionServiceTest {

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionService();
    }

    @Test
    void convertMtToMx_WithValidMT103_ShouldReturnMxXml() {
        // Given
        String mtMessage = "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4:" +
                ":20:TEST123456789" +
                ":23B:CRED" +
                ":32A:250622EUR1000,00" +
                ":50K:ORDERING CUSTOMER NAME" +
                ":59:BENEFICIARY CUSTOMER NAME" +
                ":70:PAYMENT FOR SERVICES" +
                ":71A:SHA" +
                "-}";

        // When
        String mxXml = conversionService.convertMtToMx(mtMessage);

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("<?xml version=\"1.0\"");
        assertThat(mxXml).contains("Document");
    }

    @Test
    void convertMtToMx_WithValidMT102_ShouldReturnMxXml() {
        // Given
        String mtMessage = "{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4:" +
                ":20:TEST123456789" +
                ":23:CRED" +
                ":32A:250622EUR5000,00" +
                ":50K:ORDERING CUSTOMER" +
                ":21:TXN001" +
                ":32B:EUR1000,00" +
                ":59:BENEFICIARY 1" +
                ":21:TXN002" +
                ":32B:EUR2000,00" +
                ":59:BENEFICIARY 2" +
                "-}";

        // When
        String mxXml = conversionService.convertMtToMx(mtMessage);

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("<?xml version=\"1.0\"");
        assertThat(mxXml).contains("Document");
    }

    @Test
    void convertMtToMx_WithValidMT202_ShouldReturnMxXml() {
        // Given
        String mtMessage = "{1:F01BANKBEBB0000000000}{2:I202BANKDEFFN}{4:" +
                ":20:TEST123456789" +
                ":32A:250622EUR10000,00" +
                ":58A:BANKCHZZ0000" +
                "-}";

        // When
        String mxXml = conversionService.convertMtToMx(mtMessage);

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("<?xml version=\"1.0\"");
        assertThat(mxXml).contains("Document");
    }

    @Test
    void convertMtToMx_WithNullMessage_ShouldReturnErrorXml() {
        // When
        String mxXml = conversionService.convertMtToMx(null);

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("Error");
    }

    @Test
    void convertMtToMx_WithEmptyMessage_ShouldReturnErrorXml() {
        // When
        String mxXml = conversionService.convertMtToMx("");

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("Error");
    }

    @Test
    void convertMtToMx_WithInvalidMessage_ShouldReturnErrorXml() {
        // When
        String mxXml = conversionService.convertMtToMx("INVALID MESSAGE");

        // Then
        assertThat(mxXml).isNotNull();
        assertThat(mxXml).contains("Error");
    }
}