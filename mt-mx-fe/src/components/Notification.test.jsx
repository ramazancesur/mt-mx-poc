import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import Notification from './Notification';
import theme from '../theme/theme';
import '../test/setup';

const renderWithProviders = (component) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

// ✅ Bildirim bileşeni testleri
describe('Notification Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  it('should render success notification', () => {
    renderWithProviders(
      <Notification 
        open={true} 
        message="Başarılı işlem" 
        severity="success" 
        onClose={vi.fn()} 
      />
    );
    
    expect(screen.getByText('Başarılı işlem')).toBeInTheDocument();
  });

  it('should render error notification', () => {
    renderWithProviders(
      <Notification 
        open={true} 
        message="Hata mesajı" 
        severity="error" 
        onClose={vi.fn()} 
      />
    );
    
    expect(screen.getByText('Hata mesajı')).toBeInTheDocument();
  });

  it.skip('should handle close button click', () => {
    const mockOnClose = vi.fn();

    renderWithProviders(
      <Notification 
        open={true} 
        message="Test mesajı"
        severity="info" 
        onClose={mockOnClose} 
      />
    );
    
    const closeButton = screen.getByRole('button', { name: /close/i });
    fireEvent.click(closeButton);

    expect(mockOnClose).toHaveBeenCalled();
  });

  it('should not render when open is false', () => {
    renderWithProviders(
      <Notification 
        open={false}
        message="Gizli mesaj" 
        severity="info" 
        onClose={vi.fn()} 
      />
    );
    
    expect(screen.queryByText('Gizli mesaj')).not.toBeInTheDocument();
  });

  it('should render warning notification', () => {
    renderWithProviders(
      <Notification 
        open={true}
        message="Uyarı mesajı"
        severity="warning"
        onClose={vi.fn()} 
      />
    );
    
    expect(screen.getByText('Uyarı mesajı')).toBeInTheDocument();
  });

  it.skip('should auto-hide after timeout', () => {
    vi.useFakeTimers();
    const mockOnClose = vi.fn();

    renderWithProviders(
      <Notification 
        open={true} 
        message="Auto-hide mesajı"
        severity="info"
        onClose={mockOnClose}
        autoHideDuration={3000}
      />
    );
    
    // Advance timers by 3 seconds
    vi.advanceTimersByTime(3000);

    expect(mockOnClose).toHaveBeenCalled();

    vi.useRealTimers();
  });
});
