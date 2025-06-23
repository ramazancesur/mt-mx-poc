package com.mtmx.service.validation;

import com.mtmx.domain.entity.SwiftMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation sonucu sınıfı
 */
@Getter
@Setter
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private SwiftMessage savedMessage;

    public void addError(String error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{errors=%d, warnings=%d, valid=%s}", 
                errors.size(), warnings.size(), isValid());
    }
}
