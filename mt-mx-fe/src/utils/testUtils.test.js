import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  formatDate,
  formatCurrency,
  validateMessageType,
  generateMessageId,
  parseMessageContent,
  calculateProgress,
  debounce,
  throttle
} from './testUtils';

describe('Test Utilities', () => {
  describe('formatDate', () => {
    it('should format valid date', () => {
      const date = '2024-01-01T10:00:00';
      const result = formatDate(date);
      expect(result).toBeDefined();
      expect(typeof result).toBe('string');
    });

    it('should return empty string for null date', () => {
      expect(formatDate(null)).toBe('');
    });

    it('should return empty string for undefined date', () => {
      expect(formatDate(undefined)).toBe('');
    });

    it('should return empty string for empty string', () => {
      expect(formatDate('')).toBe('');
    });
  });

  describe('formatCurrency', () => {
    it('should format currency with default USD', () => {
      const result = formatCurrency(1000);
      expect(result).toContain('$');
      expect(result).toContain('1,000');
    });

    it('should format currency with custom currency', () => {
      const result = formatCurrency(1000, 'EUR');
      expect(result).toBeDefined();
      expect(typeof result).toBe('string');
    });


    it('should handle null amount', () => {
      const result = formatCurrency(null);
      expect(result).toBe('0.00');
    });

    it('should handle undefined amount', () => {
      const result = formatCurrency(undefined);
      expect(result).toBe('0.00');
    });
  });

  describe('validateMessageType', () => {
    it('should validate MT102', () => {
      expect(validateMessageType('MT102')).toBe(true);
    });

    it('should validate MT103', () => {
      expect(validateMessageType('MT103')).toBe(true);
    });

    it('should validate MT202', () => {
      expect(validateMessageType('MT202')).toBe(true);
    });

    it('should validate MT203', () => {
      expect(validateMessageType('MT203')).toBe(true);
    });

    it('should reject invalid type', () => {
      expect(validateMessageType('MT999')).toBe(false);
    });

    it('should reject null type', () => {
      expect(validateMessageType(null)).toBe(false);
    });

    it('should reject undefined type', () => {
      expect(validateMessageType(undefined)).toBe(false);
    });

    it('should reject empty string', () => {
      expect(validateMessageType('')).toBe(false);
    });
  });

  describe('generateMessageId', () => {
    it('should generate unique message ID', () => {
      const id1 = generateMessageId();
      const id2 = generateMessageId();
      
      expect(id1).toBeDefined();
      expect(id2).toBeDefined();
      expect(id1).not.toBe(id2);
      expect(id1).toMatch(/^MSG_/);
      expect(id2).toMatch(/^MSG_/);
    });

    it('should generate ID with correct format', () => {
      const id = generateMessageId();
      expect(id).toMatch(/^MSG_\d+_[a-z0-9]+$/);
    });
  });

  describe('parseMessageContent', () => {
    it('should parse valid JSON', () => {
      const content = '{"key": "value"}';
      const result = parseMessageContent(content);
      expect(result).toEqual({ key: 'value' });
    });

    it('should handle invalid JSON', () => {
      const content = 'invalid json';
      const result = parseMessageContent(content);
      expect(result).toEqual({ raw: 'invalid json' });
    });

    it('should handle null content', () => {
      const result = parseMessageContent(null);
      expect(result).toEqual({});
    });

    it('should handle undefined content', () => {
      const result = parseMessageContent(undefined);
      expect(result).toEqual({});
    });

    it('should handle empty string', () => {
      const result = parseMessageContent('');
      expect(result).toEqual({});
    });
  });

  describe('calculateProgress', () => {
    it('should calculate progress correctly', () => {
      expect(calculateProgress(50, 100)).toBe(50);
      expect(calculateProgress(25, 100)).toBe(25);
      expect(calculateProgress(100, 100)).toBe(100);
    });

    it('should handle zero total', () => {
      expect(calculateProgress(10, 0)).toBe(0);
    });

    it('should handle null total', () => {
      expect(calculateProgress(10, null)).toBe(0);
    });

    it('should handle undefined total', () => {
      expect(calculateProgress(10, undefined)).toBe(0);
    });

    it('should round to nearest integer', () => {
      expect(calculateProgress(33, 100)).toBe(33);
      expect(calculateProgress(67, 100)).toBe(67);
    });
  });

  describe('debounce', () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('should debounce function calls', () => {
      const mockFn = vi.fn();
      const debouncedFn = debounce(mockFn, 100);

      debouncedFn();
      debouncedFn();
      debouncedFn();

      expect(mockFn).not.toHaveBeenCalled();

      vi.advanceTimersByTime(100);

      expect(mockFn).toHaveBeenCalledTimes(1);
    });

    it('should pass arguments correctly', () => {
      const mockFn = vi.fn();
      const debouncedFn = debounce(mockFn, 100);

      debouncedFn('arg1', 'arg2');

      vi.advanceTimersByTime(100);

      expect(mockFn).toHaveBeenCalledWith('arg1', 'arg2');
    });
  });

  describe('throttle', () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('should throttle function calls', () => {
      const mockFn = vi.fn();
      const throttledFn = throttle(mockFn, 100);

      throttledFn();
      throttledFn();
      throttledFn();

      expect(mockFn).toHaveBeenCalledTimes(1);

      vi.advanceTimersByTime(100);

      throttledFn();

      expect(mockFn).toHaveBeenCalledTimes(2);
    });
  });
});
