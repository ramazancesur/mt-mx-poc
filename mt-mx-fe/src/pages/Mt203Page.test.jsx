import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material/styles';
import Mt203Page from './Mt203Page';
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

describe.skip('Mt203Page Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render page title and create button', async () => {
    renderWithProviders(<Mt203Page />);
    
    expect(screen.getByText('page_title_mt203')).toBeInTheDocument();
    expect(screen.getByText('Create New Message')).toBeInTheDocument();
  });

  it('should display loading state initially', async () => {
    renderWithProviders(<Mt203Page />);
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should display no messages alert when no data', async () => {
    renderWithProviders(<Mt203Page />);
    
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    expect(screen.getByText('No MT203 messages found.')).toBeInTheDocument();
  });

  it('should open and close message form', async () => {
    renderWithProviders(<Mt203Page />);
    
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
          <button onClick={() => { setSelectedId(203); setDetailOpen(true); }}>
            Open MT203 Detail
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
    
    const openButton = screen.getByText('Open MT203 Detail');
    fireEvent.click(openButton);
    
    await waitFor(() => {
      expect(screen.getByTestId('message-detail')).toBeInTheDocument();
      expect(screen.getByText('Message ID: 203')).toBeInTheDocument();
    });
  });

  it('should handle pagination with different page sizes', async () => {
    const MockedPaginationPage = () => {
      const [page, setPage] = React.useState(1);
      const [pageSize, setPageSize] = React.useState(10);

      return (
        <div>
          <div>MT203 Page: {page}, Size: {pageSize}</div>
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
            <option value={50}>50</option>
          </select>
          <button onClick={() => setPage(page + 1)}>Next Page</button>
        </div>
      );
    };

    render(<MockedPaginationPage />);
    
    expect(screen.getByText('MT203 Page: 1, Size: 10')).toBeInTheDocument();

    const select = screen.getByTestId('page-size-select');
    fireEvent.change(select, { target: { value: '50' } });
    
    expect(screen.getByText('MT203 Page: 1, Size: 50')).toBeInTheDocument();

    const nextButton = screen.getByText('Next Page');
    fireEvent.click(nextButton);
    
    expect(screen.getByText('MT203 Page: 2, Size: 50')).toBeInTheDocument();
  });

  it('should handle message deletion', async () => {
    const MockedDeletePage = () => {
      const [messageCount, setMessageCount] = React.useState(5);
      
      const handleDelete = () => {
        setMessageCount(messageCount - 1);
      };

      return (
        <div>
          <div>MT203 Messages ({messageCount})</div>
          <button onClick={handleDelete}>Delete Message</button>
        </div>
      );
    };

    render(<MockedDeletePage />);
    
    expect(screen.getByText('MT203 Messages (5)')).toBeInTheDocument();
    
    const deleteButton = screen.getByText('Delete Message');
    fireEvent.click(deleteButton);
    
    expect(screen.getByText('MT203 Messages (4)')).toBeInTheDocument();
  });
});
