import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import { ThemeProvider } from '@mui/material/styles';
import Mt103Page from './Mt103Page';
import i18n from '../i18n';
import theme from '../theme/theme';
import { MessageProvider } from '../context/MessageContext';
import '../test/setup';

// Mock the services
vi.mock('../services/swiftMessageService', () => ({
  default: {
    getMessages: vi.fn(() => Promise.resolve({ success: true, data: { content: [], totalPages: 0, totalElements: 0 } })),
    getMessagesByType: vi.fn(() => Promise.resolve({ success: true, data: { content: [], totalPages: 0, totalElements: 0 } })),
    createMessage: vi.fn(() => Promise.resolve({ success: true, data: {}, message: 'Created' })),
    updateMessage: vi.fn(() => Promise.resolve({ success: true, data: {}, message: 'Updated' })),
    deleteMessage: vi.fn(() => Promise.resolve({ success: true, message: 'Deleted' })),
    convertMessage: vi.fn(() => Promise.resolve({ success: true, data: {}, message: 'Converted' }))
  }
}));

// Mock the components
vi.mock('../components/MessageForm', () => ({
  default: ({ open, handleClose }) => (
    open ? (
      <div data-testid="message-form">
        <button onClick={handleClose}>Close Form</button>
      </div>
    ) : null
  )
}));

vi.mock('../components/MessageDetail', () => ({
  default: ({ open, handleClose, messageId, onMessageUpdated }) => (
    open ? (
      <div data-testid="message-detail">
        <span>Message ID: {messageId}</span>
        <button onClick={handleClose}>Close Detail</button>
        <button onClick={onMessageUpdated}>Update Message</button>
      </div>
    ) : null
  )
}));

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

describe.skip('Mt103Page Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render MT103 page', async () => {
    renderWithProviders(<Mt103Page />);

    await waitFor(() => {
      expect(screen.getByText(/MT103/i)).toBeInTheDocument();
    });
  });

  it('should display empty state message when no messages', async () => {
    renderWithProviders(<Mt103Page />);

    await waitFor(() => {
      expect(screen.getByText(/no messages|mesaj yok|boş/i)).toBeInTheDocument();
    });
  });
});
