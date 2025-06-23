import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

class SwiftMessageService {
  constructor() {
    this.apiClient = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  // Get all messages with pagination
  async getAllMessages(page = 0, size = 10) {
    try {
      const response = await this.apiClient.get('/api/swift-messages', {
        params: { page, size }
      });
      // Backend zaten {success, message, data} formatında döndürüyor
      return response.data;
    } catch (error) {
      console.error('Error fetching messages:', error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Get all messages with pagination (alias for backward compatibility)
  async getMessages(page = 0, size = 10) {
    return this.getAllMessages(page, size);
  }

  // Get messages by type
  async getMessagesByType(messageType, page = 0, size = 10) {
    try {
      const response = await this.apiClient.get(`/api/swift-messages/type/${messageType}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.error(`Error fetching ${messageType} messages:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Get message by ID
  async getMessageById(id) {
    try {
      const response = await this.apiClient.get(`/api/swift-messages/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching message ${id}:`, error);
      throw error;
    }
  }

  // Create new message
  async createMessage(messageData) {
    try {
      const response = await this.apiClient.post('/api/swift-messages', messageData);
      return response.data;
    } catch (error) {
      console.error('Error creating message:', error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Update message
  async updateMessage(id, messageData) {
    try {
      const response = await this.apiClient.put(`/api/swift-messages/${id}`, messageData);
      return response.data;
    } catch (error) {
      console.error(`Error updating message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Delete message
  async deleteMessage(id) {
    try {
      const response = await this.apiClient.delete(`/api/swift-messages/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error deleting message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Convert message
  async convertMessage(id) {
    try {
      const response = await this.apiClient.post(`/api/swift-messages/${id}/convert`, '');
      return response.data;
    } catch (error) {
      console.error(`Error converting message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Convert MT to MX message
  async convertMtToMx(id, rawMtMessage = null) {
    try {
      const response = await this.apiClient.post(`/api/swift-messages/${id}/convert`, rawMtMessage || '');
      return response.data;
    } catch (error) {
      console.error(`Error converting MT to MX message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Convert MX to MT message
  async convertMxToMt(id) {
    try {
      const response = await this.apiClient.post(`/api/swift-messages/${id}/convert-mx-to-mt`);
      return response.data;
    } catch (error) {
      console.error(`Error converting MX to MT message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Update XML content
  async updateXmlContent(id, xmlContent) {
    try {
      const response = await this.apiClient.put(`/api/swift-messages/${id}/update-xml`, xmlContent, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      console.error(`Error updating XML content for message ${id}:`, error);
      return { success: false, message: error.message, data: null };
    }
  }

  // Health check
  async healthCheck() {
    try {
      const response = await this.apiClient.get('/api/health');
      return response.data;
    } catch (error) {
      console.error('Health check failed:', error);
      throw error;
    }
  }
}

export default new SwiftMessageService();
