package com.mtmx.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTypeTest {

    @Test
    void messageType_ShouldHaveAllExpectedValues() {
        // When
        MessageType[] values = MessageType.values();

        // Then
        assertThat(values).hasSize(5);
        assertThat(values).contains(
                MessageType.MT102,
                MessageType.MT103,
                MessageType.MT202,
                MessageType.MT202COV,
                MessageType.MT203
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When & Then
        assertThat(MessageType.valueOf("MT102")).isEqualTo(MessageType.MT102);
        assertThat(MessageType.valueOf("MT103")).isEqualTo(MessageType.MT103);
        assertThat(MessageType.valueOf("MT202")).isEqualTo(MessageType.MT202);
        assertThat(MessageType.valueOf("MT202COV")).isEqualTo(MessageType.MT202COV);
        assertThat(MessageType.valueOf("MT203")).isEqualTo(MessageType.MT203);
    }

    @Test
    void toString_ShouldReturnCorrectString() {
        // When & Then
        assertThat(MessageType.MT102.toString()).isEqualTo("MT102");
        assertThat(MessageType.MT103.toString()).isEqualTo("MT103");
        assertThat(MessageType.MT202.toString()).isEqualTo("MT202");
        assertThat(MessageType.MT202COV.toString()).isEqualTo("MT202COV");
        assertThat(MessageType.MT203.toString()).isEqualTo("MT203");
    }
}
