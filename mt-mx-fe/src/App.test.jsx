import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import { ThemeProvider } from '@mui/material/styles';
import { MessageProvider } from './context/MessageContext';
import Layout from './components/Layout';
import i18n from './i18n';
import theme from './theme/theme';
import { MemoryRouter } from 'react-router-dom';

vi.mock('./services/swiftMessageService');

const renderWithProviders = (children, initialEntries = ['/']) => {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <I18nextProvider i18n={i18n}>
        <ThemeProvider theme={theme}>
          <MessageProvider>
            {children}
          </MessageProvider>
        </ThemeProvider>
      </I18nextProvider>
    </MemoryRouter>
  );
};

describe('App Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render Layout component without crashing', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Layout should render successfully
    expect(screen.getByRole('banner')).toBeInTheDocument(); // AppBar
    expect(screen.getByRole('main')).toBeInTheDocument(); // Main content area
  });

  it('should render navigation menu', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Check for navigation menu items
    expect(screen.getByText(/MT102/i)).toBeInTheDocument();
    expect(screen.getByText(/MT103/i)).toBeInTheDocument();
    expect(screen.getByText(/MT202/i)).toBeInTheDocument();
    expect(screen.getByText(/MT203/i)).toBeInTheDocument();
  });

  it('should render language selection buttons', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Language buttons should be present
    expect(screen.getByText('EN')).toBeInTheDocument();
    expect(screen.getByText('TR')).toBeInTheDocument();
  });

  it('should render the app title', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // App title should be present
    expect(screen.getByText(/MT-MX|app_title/i)).toBeInTheDocument();
  });

  it('should render children content', () => {
    renderWithProviders(<Layout><div data-testid="test-content">Test Content</div></Layout>);
    
    // Children should be rendered
    expect(screen.getByTestId('test-content')).toBeInTheDocument();
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('should provide theme correctly', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // Theme should be applied (check for MUI components)
    expect(screen.getByRole('banner')).toBeInTheDocument();
  });

  it('should provide message context', () => {
    renderWithProviders(<Layout><div>Test Content</div></Layout>);
    
    // MessageProvider should be working (no errors)
    expect(screen.getByRole('banner')).toBeInTheDocument();
  });
});
