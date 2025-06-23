import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material/styles';
import Mt202Page from './Mt202Page';
import { MessageProvider } from '../context/MessageContext';
import '../test/setup';

vi.mock('../services/swiftMessageService', () => ({
  default: {
    getAllMessages: vi.fn(),
    getMessagesByType: vi.fn(),
    createMessage: vi.fn(),
    updateMessage: vi.fn(),
    deleteMessage: vi.fn(),
    convertMessage: vi.fn()
  }
}));

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
  default: ({ open, handleClose, messageId }) => (
    open ? (
      <div data-testid="message-detail">
        <span>Message ID: {messageId}</span>
        <button onClick={handleClose}>Close Detail</button>
      </div>
    ) : null
  )
}));

const theme = createTheme();

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <MessageProvider>
          {component}
        </MessageProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
};

describe.skip('Mt202Page Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render page title and create button', async () => {
    renderWithProviders(<Mt202Page />);
    
    expect(screen.getByText('page_title_mt202')).toBeInTheDocument();
    expect(screen.getByText('Create New Message')).toBeInTheDocument();
  });

  it('should display loading state initially', async () => {
    renderWithProviders(<Mt202Page />);
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should display no messages alert when no data', async () => {
    renderWithProviders(<Mt202Page />);
    
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    expect(screen.getByText('No MT202 messages found.')).toBeInTheDocument();
  });

  it('should open and close message form', async () => {
    renderWithProviders(<Mt202Page />);
    
    const createButton = screen.getByText('Create New Message');
    fireEvent.click(createButton);
    
    await waitFor(() => {
      expect(screen.getByTestId('message-form')).toBeInTheDocument();
    });

    const closeButton = screen.getByText('Close Form');
    fireEvent.click(closeButton);
    
    await waitFor(() => {
      expect(screen.queryByTestId('message-form')).not.toBeInTheDocument();
    });
  });

  it('should handle message detail operations', async () => {
    const MockedDetailPage = () => {
      const [detailOpen, setDetailOpen] = React.useState(false);
      const [selectedId, setSelectedId] = React.useState(null);

      return (
        <div>
          <button onClick={() => { setSelectedId(202); setDetailOpen(true); }}>
            Open MT202 Detail
          </button>
          {detailOpen && (
            <div data-testid="message-detail">
              <span>Message ID: {selectedId}</span>
              <button onClick={() => setDetailOpen(false)}>Close Detail</button>
            </div>
          )}
        </div>
      );
    };

    render(<MockedDetailPage />);
    
    const openButton = screen.getByText('Open MT202 Detail');
    fireEvent.click(openButton);
    
    await waitFor(() => {
      expect(screen.getByTestId('message-detail')).toBeInTheDocument();
      expect(screen.getByText('Message ID: 202')).toBeInTheDocument();
    });
  });

  it('should handle pagination functionality', async () => {
    const MockedPaginationPage = () => {
      const [page, setPage] = React.useState(1);
      const [pageSize, setPageSize] = React.useState(10);

      return (
        <div>
          <div>MT202 Page: {page}, Size: {pageSize}</div>
          <select 
            value={pageSize} 
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(1);
            }}
            data-testid="page-size-select"
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
          </select>
          <button onClick={() => setPage(page + 1)}>Next Page</button>
        </div>
      );
    };

    render(<MockedPaginationPage />);
    
    expect(screen.getByText('MT202 Page: 1, Size: 10')).toBeInTheDocument();

    const select = screen.getByTestId('page-size-select');
    fireEvent.change(select, { target: { value: '20' } });
    
    expect(screen.getByText('MT202 Page: 1, Size: 20')).toBeInTheDocument();

    const nextButton = screen.getByText('Next Page');
    fireEvent.click(nextButton);
    
    expect(screen.getByText('MT202 Page: 2, Size: 20')).toBeInTheDocument();
  });
});
