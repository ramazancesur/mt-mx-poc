import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import { ThemeProvider } from '@mui/material/styles';
import { MessageProvider } from '../context/MessageContext';
import Layout from './Layout';
import i18n from '../i18n';
import theme from '../theme/theme';

vi.mock('../services/swiftMessageService');

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <I18nextProvider i18n={i18n}>
        <ThemeProvider theme={theme}>
          <MessageProvider>
            {component}
          </MessageProvider>
        </ThemeProvider>
      </I18nextProvider>
    </BrowserRouter>
  );
};

describe('Layout Component Simple Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render the app title', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // App title should be present
    expect(screen.getByText('app_title')).toBeInTheDocument();
  });

  it('should render menu items', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Menu items should be present
    expect(screen.getByText('menu_mt102')).toBeInTheDocument();
    expect(screen.getByText('menu_mt103')).toBeInTheDocument();
    expect(screen.getByText('menu_mt202')).toBeInTheDocument();
    expect(screen.getByText('menu_mt203')).toBeInTheDocument();
  });

  it('should render language selection buttons', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Language buttons should be present
    expect(screen.getByText('EN')).toBeInTheDocument();
    expect(screen.getByText('TR')).toBeInTheDocument();
  });

  it('should render children content', () => {
    renderWithProviders(<Layout><div data-testid="test-child">Test Content</div></Layout>);
    
    // Children should be rendered
    expect(screen.getByTestId('test-child')).toBeInTheDocument();
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('should handle language change', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    const trButton = screen.getByText('TR');
    const enButton = screen.getByText('EN');
    
    // Buttons should be clickable
    fireEvent.click(trButton);
    fireEvent.click(enButton);
    
    // No errors should occur
    expect(trButton).toBeInTheDocument();
    expect(enButton).toBeInTheDocument();
  });

  it('should have main content area', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Main content area should exist
    const main = screen.getByRole('main');
    expect(main).toBeInTheDocument();
  });

  it('should have proper structure', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Basic structure elements should exist
    expect(screen.getByRole('banner')).toBeInTheDocument(); // AppBar
    expect(screen.getByRole('main')).toBeInTheDocument(); // Main content
  });
});
