// Test utility functions
export const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString();
};

export const formatCurrency = (amount, currency = 'USD') => {
  if (!amount) return '0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount);
};

export const validateMessageType = (type) => {
  const validTypes = ['MT102', 'MT103', 'MT202', 'MT203'];
  return validTypes.includes(type);
};

export const generateMessageId = () => {
  return `MSG_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

export const parseMessageContent = (content) => {
  if (!content) return {};
  
  try {
    return JSON.parse(content);
  } catch (e) {
    return { raw: content };
  }
};

export const calculateProgress = (current, total) => {
  if (!total || total === 0) return 0;
  return Math.round((current / total) * 100);
};

export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

export const throttle = (func, limit) => {
  let inThrottle;
  return function() {
    const args = arguments;
    const context = this;
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};
