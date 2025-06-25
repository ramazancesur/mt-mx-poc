import axios from 'axios';

// API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';
const API_ENDPOINTS = {
    MESSAGES: '/api/swift-messages',
    CONVERT: '/api/convert',
    VALIDATE: '/api/validate',
    HEALTH: '/api/health'
};

// Create full URL for endpoints
const createUrl = (endpoint) => `${API_BASE_URL}${endpoint}`;

// Test için mock response helper
const createMockResponse = (data, message = 'Success') => ({
    success: true,
    message,
    data
});

// Network error handler
const handleNetworkError = (error) => {
    console.error('API Network Error:', error);
    
    // Connection refused durumunda backend'in çalışmadığını belirt
    if (error.code === 'ECONNREFUSED' || error.message?.includes('ERR_CONNECTION_REFUSED')) {
        throw new Error('Backend service is not running. Please start the backend.');
    }
    
    // CORS hataları için
    if (error.message?.includes('CORS')) {
        throw new Error('CORS error. Please check backend CORS configuration.');
    }
    
    throw error;
};

class SwiftMessageService {
  // Health check - backend'in çalışıp çalışmadığını kontrol et
  static async checkHealth() {
    try {
      const response = await fetch(createUrl(API_ENDPOINTS.HEALTH), {
        method: 'GET',
      headers: {
          'Accept': 'application/json'
        }
    });
      
      if (!response.ok) {
        throw new Error(`Health check failed: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      return {
        success: true,
        data,
        message: 'Backend is running healthy'
      };
    } catch (error) {
      console.error('Health check failed:', error);
      return {
        success: false,
        message: 'Backend service is not accessible',
        error: error.message
      };
    }
  }

  // Get all messages with pagination
  static async getMessages(page = 0, size = 10) {
    try {
      const url = createUrl(`${API_ENDPOINTS.MESSAGES}?page=${page}&size=${size}`);
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data; // Backend zaten doğru format döndürüyor
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Get messages by type
  static async getMessagesByType(messageType, page = 0, size = 10) {
    try {
      const url = createUrl(`${API_ENDPOINTS.MESSAGES}/type/${messageType}?page=${page}&size=${size}`);
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data; // Backend zaten doğru format döndürüyor
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Get message by ID
  static async getMessageById(id) {
    try {
      const url = createUrl(`${API_ENDPOINTS.MESSAGES}/${id}`);
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error(`Error fetching message ${id}:`, error);
      handleNetworkError(error);
    }
  }

  // Create new message
  static async createMessage(messageData) {
    try {
      const response = await fetch(createUrl(API_ENDPOINTS.MESSAGES), {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageData)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Update message
  static async updateMessage(id, messageData) {
    try {
      const response = await fetch(createUrl(`${API_ENDPOINTS.MESSAGES}/${id}`), {
        method: 'PUT',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageData)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Delete message
  static async deleteMessage(id) {
    try {
      const response = await fetch(createUrl(`${API_ENDPOINTS.MESSAGES}/${id}`), {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Upload message file
  static async uploadMessageFile(formData) {
    try {
      const response = await fetch(createUrl(`${API_ENDPOINTS.MESSAGES}/upload`), {
        method: 'POST',
        body: formData // FormData otomatik olarak Content-Type header'ını ayarlar
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Convert message
  static async convertMessage(id) {
    try {
      const response = await fetch(createUrl(`${API_ENDPOINTS.MESSAGES}/${id}/convert`), {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
    }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Validate message
  static async validateMessage(messageData) {
    try {
      const response = await fetch(createUrl(API_ENDPOINTS.VALIDATE), {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(messageData)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
      
    } catch (error) {
      handleNetworkError(error);
    }
  }

  // Get API Info
  static getApiInfo() {
    return {
      baseUrl: API_BASE_URL,
      endpoints: API_ENDPOINTS,
      fullUrls: {
        messages: createUrl(API_ENDPOINTS.MESSAGES),
        convert: createUrl(API_ENDPOINTS.CONVERT),
        validate: createUrl(API_ENDPOINTS.VALIDATE),
        health: createUrl(API_ENDPOINTS.HEALTH)
      }
    };
  }
}

// Default export as singleton instance
export default SwiftMessageService;
