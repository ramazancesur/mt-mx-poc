import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, act, waitFor } from '@testing-library/react';
import { MessageProvider, useMessages } from './MessageContext';
import SwiftMessageService from '../services/swiftMessageService';

vi.mock('../services/swiftMessageService');

const TestComponent = () => {
  const { 
    messagePage, 
    loading, 
    notification,
    fetchMessages, 
    fetchMessagesByType,
    addMessage,
    updateMessage,
    deleteMessage,
    convertMessage,
    closeNotification
  } = useMessages();
  
  return (
    <div>
      <div data-testid="loading">{loading ? 'Loading' : 'Not Loading'}</div>
      <div data-testid="notification-open">{notification.open ? 'Open' : 'Closed'}</div>
      <div data-testid="notification-message">{notification.message}</div>
      <div data-testid="message-count">{messagePage.content.length}</div>
      <button onClick={() => fetchMessages(0, 10)} data-testid="fetch-messages">
        Fetch Messages
      </button>
      <button onClick={() => fetchMessagesByType('MT103', 0, 10)} data-testid="fetch-by-type">
        Fetch By Type
      </button>
      <button onClick={() => addMessage({ messageType: 'MT103', content: 'test' })} data-testid="add-message">
        Add Message
      </button>
      <button onClick={() => updateMessage(1, { messageType: 'MT103', content: 'updated' })} data-testid="update-message">
        Update Message
      </button>
      <button onClick={() => deleteMessage(1)} data-testid="delete-message">
        Delete Message
      </button>
      <button onClick={() => convertMessage(1)} data-testid="convert-message">
        Convert Message
      </button>
      <button onClick={closeNotification} data-testid="close-notification">
        Close Notification
      </button>
    </div>
  );
};

describe.skip('MessageContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    SwiftMessageService.getAllMessages.mockResolvedValue({
      success: true,
      data: { content: [], totalPages: 0, totalElements: 0 }
    });
    
    SwiftMessageService.getMessagesByType.mockResolvedValue({
      success: true,
      data: { content: [], totalPages: 0, totalElements: 0 }
    });
    
    SwiftMessageService.createMessage.mockResolvedValue({
      success: true,
      data: { id: 1, messageType: 'MT103', content: 'test' }
    });
    
    SwiftMessageService.updateMessage.mockResolvedValue({
      success: true,
      data: { id: 1, messageType: 'MT103', content: 'updated' }
    });
    
    SwiftMessageService.deleteMessage.mockResolvedValue({
      success: true
    });
    
    SwiftMessageService.convertMessage.mockResolvedValue({
      success: true,
      data: { convertedMessage: 'converted content' }
    });
  });

  it('should provide initial state', () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
    expect(screen.getByTestId('notification-open')).toHaveTextContent('Closed');
    expect(screen.getByTestId('message-count')).toHaveTextContent('0');
  });

  it('should fetch messages', async () => {
    const mockMessages = [
      { id: 1, messageType: 'MT103', content: 'test1' },
      { id: 2, messageType: 'MT102', content: 'test2' }
    ];
    
    SwiftMessageService.getAllMessages.mockResolvedValue({
      success: true,
      data: { content: mockMessages, totalPages: 1, totalElements: 2 }
    });
    
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('fetch-messages').click();
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('message-count')).toHaveTextContent('2');
    });
    
    expect(SwiftMessageService.getAllMessages).toHaveBeenCalledWith(0, 10);
  });

  it('should fetch messages by type', async () => {
    const mockMessages = [
      { id: 1, messageType: 'MT103', content: 'test1' }
    ];
    
    SwiftMessageService.getMessagesByType.mockResolvedValue({
      success: true,
      data: { content: mockMessages, totalPages: 1, totalElements: 1 }
    });
    
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('fetch-by-type').click();
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('message-count')).toHaveTextContent('1');
    });
    
    expect(SwiftMessageService.getMessagesByType).toHaveBeenCalledWith('MT103', 0, 10);
  });

  it('should add message and show success notification', async () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('add-message').click();
    });
    
    await waitFor(() => {
      expect(SwiftMessageService.createMessage).toHaveBeenCalledWith({
        messageType: 'MT103',
        content: 'test'
      });
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-message')).toHaveTextContent('Mesaj başarıyla oluşturuldu!');
    });
  });

  it('should update message and show success notification', async () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('update-message').click();
    });
    
    await waitFor(() => {
      expect(SwiftMessageService.updateMessage).toHaveBeenCalledWith(1, {
        messageType: 'MT103',
        content: 'updated'
      });
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-message')).toHaveTextContent('Mesaj başarıyla güncellendi!');
    });
  });

  it('should delete message and show success notification', async () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('delete-message').click();
    });
    
    await waitFor(() => {
      expect(SwiftMessageService.deleteMessage).toHaveBeenCalledWith(1);
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-message')).toHaveTextContent('Mesaj başarıyla silindi!');
    });
  });

  it('should convert message and show success notification', async () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('convert-message').click();
    });
    
    await waitFor(() => {
      expect(SwiftMessageService.convertMessage).toHaveBeenCalledWith(1);
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-message')).toHaveTextContent('Mesaj başarıyla dönüştürüldü!');
    });
  });

  it('should close notification', async () => {
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    // First trigger a notification
    act(() => {
      screen.getByTestId('add-message').click();
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('Open');
    });
    
    // Then close it
    act(() => {
      screen.getByTestId('close-notification').click();
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-open')).toHaveTextContent('Closed');
    });
  });

  it('should handle errors gracefully', async () => {
    SwiftMessageService.getAllMessages.mockResolvedValue({
      success: false,
      error: 'Network error'
    });
    
    render(
      <MessageProvider>
        <TestComponent />
      </MessageProvider>
    );
    
    act(() => {
      screen.getByTestId('fetch-messages').click();
    });
    
    await waitFor(() => {
      expect(screen.getByTestId('notification-message')).toHaveTextContent('Network error');
    });
  });
});
