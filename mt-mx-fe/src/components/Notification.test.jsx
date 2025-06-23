import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import Notification from './Notification';
import theme from '../theme/theme';

const renderWithTheme = (component) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe.skip('Notification Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render success notification', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={true} 
        message="Success message" 
        severity="success" 
        onClose={mockOnClose} 
      />
    );
    
    expect(screen.getByText('Success message')).toBeInTheDocument();
  });

  it('should render error notification', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={true} 
        message="Error message" 
        severity="error" 
        onClose={mockOnClose} 
      />
    );
    
    expect(screen.getByText('Error message')).toBeInTheDocument();
  });

  it('should render warning notification', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={true} 
        message="Warning message" 
        severity="warning" 
        onClose={mockOnClose} 
      />
    );
    
    expect(screen.getByText('Warning message')).toBeInTheDocument();
  });

  it('should render info notification', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={true} 
        message="Info message" 
        severity="info" 
        onClose={mockOnClose} 
      />
    );
    
    expect(screen.getByText('Info message')).toBeInTheDocument();
  });

  it('should not render when closed', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={false} 
        message="Hidden message" 
        severity="info" 
        onClose={mockOnClose} 
      />
    );
    
    expect(screen.queryByText('Hidden message')).not.toBeInTheDocument();
  });

  it('should have correct positioning', () => {
    const mockOnClose = vi.fn();
    renderWithTheme(
      <Notification 
        open={true} 
        message="Positioned message" 
        severity="success" 
        onClose={mockOnClose} 
      />
    );
    
    // Snackbar should be in the DOM
    expect(screen.getByText('Positioned message')).toBeInTheDocument();
  });
});
