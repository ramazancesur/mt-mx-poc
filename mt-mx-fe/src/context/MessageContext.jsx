import React, { createContext, useState, useContext, useCallback } from 'react';
import SwiftMessageService from '../services/swiftMessageService';

const MessageContext = createContext();

export const useMessages = () => {
    const context = useContext(MessageContext);
    if (!context) {
        throw new Error('useMessages must be used within a MessageProvider');
    }
    return context;
};

export const MessageProvider = ({ children }) => {
    const [messagePage, setMessagePage] = useState({
        content: [],
        totalPages: 0,
        totalElements: 0,
        size: 10,
        number: 0
    });
    const [loading, setLoading] = useState(false);
    const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });

    const handleError = (err) => {
        const message = err.response?.data?.message || err.message || 'Beklenmeyen bir hata oluştu.';
        setNotification({ open: true, message, severity: 'error' });
    };

    const fetchMessages = useCallback(async (page = 0, size = 10) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.getMessages(page, size);
            if (response && response.success) {
                setMessagePage(response.data || { content: [], totalPages: 0, totalElements: 0 });
            } else {
                handleError(new Error(response?.message || 'Mesajlar yüklenemedi'));
            }
        } catch (err) {
            console.error('Fetch messages error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchMessagesByType = useCallback(async (messageType, page = 0, size = 10) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.getMessagesByType(messageType, page, size);
            if (response && response.success) {
                setMessagePage(response.data || { content: [], totalPages: 0, totalElements: 0 });
            } else {
                handleError(new Error(response?.message || `${messageType} mesajları yüklenemedi`));
            }
        } catch (err) {
            console.error('Fetch messages by type error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    }, []);

    const addMessage = async (message) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.createMessage(message);
            if (response && response.success) {
                setNotification({
                    open: true,
                    message: response.message || 'Mesaj başarıyla oluşturuldu!',
                    severity: 'success'
                });
                // Listeyi yenile - mevcut sayfa bilgilerini koru
                const currentPage = messagePage.number || 0;
                const currentSize = messagePage.size || 10;
                await fetchMessages(currentPage, currentSize);
            } else {
                handleError(new Error(response?.message || 'Mesaj oluşturulamadı'));
            }
        } catch (err) {
            console.error('Add message error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    };

    const updateMessage = async (id, message) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.updateMessage(id, message);
            if (response && response.success) {
                setNotification({
                    open: true,
                    message: response.message || 'Mesaj başarıyla güncellendi!',
                    severity: 'success'
                });
                // Listeyi yenile - mevcut sayfa bilgilerini koru
                const currentPage = messagePage.number || 0;
                const currentSize = messagePage.size || 10;
                await fetchMessages(currentPage, currentSize);
            } else {
                handleError(new Error(response?.message || 'Mesaj güncellenemedi'));
            }
        } catch (err) {
            console.error('Update message error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    };

    const deleteMessage = async (id) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.deleteMessage(id);
            if (response && response.success) {
                setNotification({
                    open: true,
                    message: response.message || 'Mesaj başarıyla silindi!',
                    severity: 'success'
                });
                // Listeyi yenile - mevcut sayfa bilgilerini koru
                const currentPage = messagePage.number || 0;
                const currentSize = messagePage.size || 10;
                await fetchMessages(currentPage, currentSize);
            } else {
                handleError(new Error(response?.message || 'Mesaj silinemedi'));
            }
        } catch (err) {
            console.error('Delete message error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    };

    // removeMessage alias for deleteMessage (backward compatibility)
    const removeMessage = deleteMessage;

    const convertMessage = async (id) => {
        setLoading(true);
        try {
            const response = await SwiftMessageService.convertMessage(id);
            if (response && response.success) {
                setNotification({
                    open: true,
                    message: response.message || 'Mesaj başarıyla dönüştürüldü!',
                    severity: 'success'
                });
                // Listeyi yenile - mevcut sayfa bilgilerini koru
                const currentPage = messagePage.number || 0;
                const currentSize = messagePage.size || 10;
                await fetchMessages(currentPage, currentSize);
            } else {
                handleError(new Error(response?.message || 'Mesaj dönüştürülemedi'));
            }
        } catch (err) {
            console.error('Convert message error:', err);
            handleError(err);
        } finally {
            setLoading(false);
        }
    };

    const closeNotification = () => {
        setNotification({ open: false, message: '', severity: 'info' });
    };

    const value = {
        messagePage,
        loading,
        notification,
        closeNotification,
        fetchMessages,
        fetchMessagesByType,
        addMessage,
        updateMessage,
        deleteMessage,
        removeMessage, // Alias for deleteMessage
        convertMessage
    };

    return (
        <MessageContext.Provider value={value}>
            {children}
        </MessageContext.Provider>
    );
};
