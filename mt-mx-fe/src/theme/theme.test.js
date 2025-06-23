import { describe, it, expect } from 'vitest';
import theme from './theme';

describe('Theme Configuration', () => {
  it('should have primary color defined', () => {
    expect(theme.palette.primary).toBeDefined();
    expect(theme.palette.primary.main).toBeDefined();
  });

  it('should have secondary color defined', () => {
    expect(theme.palette.secondary).toBeDefined();
    expect(theme.palette.secondary.main).toBeDefined();
  });

  it('should have typography configuration', () => {
    expect(theme.typography).toBeDefined();
    expect(theme.typography.fontFamily).toBeDefined();
  });

  it('should have spacing configuration', () => {
    expect(theme.spacing).toBeDefined();
    expect(typeof theme.spacing).toBe('function');
  });

  it('should have breakpoints configuration', () => {
    expect(theme.breakpoints).toBeDefined();
    expect(theme.breakpoints.values).toBeDefined();
  });

  it('should have shape configuration', () => {
    expect(theme.shape).toBeDefined();
    expect(theme.shape.borderRadius).toBeDefined();
  });

  it('should be a valid MUI theme object', () => {
    expect(theme).toBeDefined();
    expect(typeof theme).toBe('object');
  });
});
