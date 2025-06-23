import '@testing-library/jest-dom';
import { vi, beforeEach, afterEach, expect } from 'vitest';
import { cleanup } from '@testing-library/react';
import * as matchers from '@testing-library/jest-dom/matchers';
import 'cross-fetch/polyfill';

// i18n'i import et
import '../i18n';

// Extend Vitest's expect with jest-dom matchers
expect.extend(matchers);

// Environment variables for testing
Object.defineProperty(window, 'import.meta', {
  value: {
    env: {
      VITE_API_BASE_URL: 'http://localhost:8080'
    }
  },
  writable: true
});

// Mock global objects
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

global.fetch = vi.fn();

// Mock URL.createObjectURL
global.URL.createObjectURL = vi.fn();

// Mock window location for i18n
const mockLocation = {
  pathname: '/en',
  search: '',
  hash: '',
  state: null,
  key: 'default'
};

// Mock window location
Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true
});

// Mock environment variables for tests
process.env.REACT_APP_API_URL = 'http://localhost:8081';

// Store original console.warn
const originalWarn = console.warn;

// Mock console.warn for cleaner test output
beforeEach(() => {
  console.warn = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('validateDOMNesting') || 
       args[0].includes('Warning:') ||
       args[0].includes('act('))
    ) {
      return;
    }
    originalWarn(...args);
  };
});

// runs a cleanup after each test case (e.g. clearing jsdom)
afterEach(() => {
  cleanup();
  console.warn = originalWarn;
});

// Mock environment variables
Object.defineProperty(import.meta, 'env', {
  value: {
    VITE_API_BASE_URL: 'http://localhost:8081',
    MODE: 'test'
  },
  writable: true
});

// Mock process.env for Node.js compatibility
global.process = global.process || {};
global.process.env = global.process.env || {};
global.process.env.REACT_APP_API_URL = 'http://localhost:8081';

// Mock react-i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key) => {
      const translations = {
        'd3tree.noData': 'No XML data available',
        'd3tree.theme': 'Theme',
        'd3tree.searchPlaceholder': 'Search nodes...',
        'd3tree.zoomIn': 'Zoom In',
        'd3tree.zoomOut': 'Zoom Out',
        'd3tree.resetZoom': 'Reset Zoom',
        'd3tree.expandAll': 'Expand All',
        'd3tree.collapseAll': 'Collapse All',
        'd3tree.saveChanges': 'Save Changes',
        'd3tree.confirmSave': 'Confirm Save',
        'd3tree.confirmSaveMessage': 'Are you sure you want to save all changes?',
        'buttons.cancel': 'Cancel',
        'buttons.confirm': 'Confirm',
        'buttons.save': 'Save'
      };
      return translations[key] || key;
    },
    i18n: {
      changeLanguage: vi.fn(),
      language: 'tr'
    }
  }),
  I18nextProvider: ({ children }) => children,
  initReactI18next: {
    type: '3rdParty',
    init: vi.fn()
  }
}));

// Mock MessageContext
vi.mock('../context/MessageContext', () => ({
  useMessages: () => ({
    messages: [],
    messagePage: {
      content: [],
      totalElements: 0,
      totalPages: 0
    },
    loading: false,
    notification: {
      open: false,
      message: '',
      severity: 'info'
    },
    addMessage: vi.fn(),
    updateMessage: vi.fn(),
    deleteMessage: vi.fn(),
    convertMessage: vi.fn(),
    fetchMessages: vi.fn(),
    fetchMessagesByType: vi.fn(),
    closeNotification: vi.fn()
  }),
  MessageProvider: ({ children }) => children
}));

// Mock SwiftMessageService - Completely mock the entire module
vi.mock('../services/swiftMessageService', () => {
  const mockService = {
    fetchMessages: vi.fn(() => Promise.resolve({
      success: true,
      data: {
        content: [{ id: 1, messageType: 'MT103' }],
        totalElements: 1,
        totalPages: 1
      }
    })),
    getAllMessages: vi.fn(() => Promise.resolve({
      success: true,
      data: {
        content: [{ id: 1, messageType: 'MT103' }],
        totalElements: 1,
        totalPages: 1
      }
    })),
    getMessagesByType: vi.fn((type) => {
      if (type === 'MT999') {
        return Promise.resolve({
          success: false,
          message: 'Type not found'
        });
      }
      return Promise.resolve({
        success: true,
        data: {
          content: [{ id: 1, messageType: 'MT103' }],
          totalElements: 1,
          totalPages: 1
        }
      });
    }),
    fetchMessagesByType: vi.fn(() => Promise.resolve({
      success: true,
      data: {
        content: [],
        totalElements: 0
      }
    })),
    createMessage: vi.fn((data) => {
      if (data.rawMtMessage === 'invalid') {
        return Promise.resolve({
          success: false,
          message: 'Validation error'
        });
      }
      return Promise.resolve({
        success: true,
        message: 'Mesaj başarıyla oluşturuldu!',
        data: { id: 1, messageType: 'MT103', content: 'test' }
      });
    }),
    updateMessage: vi.fn((id, data) => {
      if (id === 999) {
        return Promise.resolve({
          success: false,
          message: 'Not found'
        });
      }
      return Promise.resolve({
        success: true,
        message: 'Mesaj başarıyla güncellendi!',
        data: { id: 1, messageType: 'MT103', content: 'updated' }
      });
    }),
    deleteMessage: vi.fn((id) => {
      if (id === 999) {
        return Promise.resolve({
          success: false,
          message: 'Not found'
        });
      }
      return Promise.resolve({
        success: true,
        message: 'Mesaj başarıyla silindi!'
      });
    }),
    convertMessage: vi.fn((id) => {
      if (id === 999) {
        return Promise.resolve({
          success: false,
          message: 'Conversion failed'
        });
      }
      return Promise.resolve({
        success: true,
        message: 'Mesaj başarıyla dönüştürüldü!',
        data: { convertedMessage: 'converted content' }
      });
    }),
    convertMtToMx: vi.fn(() => Promise.resolve({
      success: true,
      data: { generatedMxMessage: '<xml>test</xml>' }
    })),
    updateXmlContent: vi.fn(() => Promise.resolve({
      success: true,
      message: 'XML başarıyla güncellendi!'
    })),
    apiClient: {
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn()
    }
  };
  
  return {
    default: mockService,
    ...mockService
  };
});

// Mock D3 completely
vi.mock('d3', () => ({
  select: vi.fn(() => ({
    append: vi.fn(() => ({
      attr: vi.fn(() => ({
        style: vi.fn(() => ({
          call: vi.fn(),
          text: vi.fn(),
          on: vi.fn()
        }))
      })),
      style: vi.fn(() => ({
        attr: vi.fn(() => ({
          style: vi.fn()
        }))
      }))
    })),
    selectAll: vi.fn(() => ({
      remove: vi.fn(),
      data: vi.fn(() => ({
        enter: vi.fn(() => ({
          append: vi.fn(() => ({
            attr: vi.fn(() => ({
              style: vi.fn(() => ({
                on: vi.fn(() => ({
                  append: vi.fn(() => ({
                    attr: vi.fn(() => ({
                      style: vi.fn(() => ({
                        text: vi.fn()
                      }))
                    }))
                  }))
                }))
              }))
            }))
          }))
        }))
      }))
    })),
    style: vi.fn(() => ({
      attr: vi.fn(),
      call: vi.fn(),
      transition: vi.fn(() => ({
        call: vi.fn(),
        duration: vi.fn(() => ({
          call: vi.fn()
        }))
      }))
    })),
    call: vi.fn(),
    transition: vi.fn(() => ({
      call: vi.fn(),
      duration: vi.fn(() => ({
        call: vi.fn()
      }))
    })),
    node: vi.fn(() => ({
      getBBox: vi.fn(() => ({
        x: 0,
        y: 0,
        width: 100,
        height: 100
      }))
    }))
  })),
  hierarchy: vi.fn((data) => ({
    data,
    children: data?.children || null,
    descendants: vi.fn(() => [{ data }]),
    links: vi.fn(() => [])
  })),
  tree: vi.fn(() => ({
    size: vi.fn(() => vi.fn())
  })),
  linkHorizontal: vi.fn(() => ({
    x: vi.fn(() => ({
      y: vi.fn()
    }))
  })),
  zoom: vi.fn(() => ({
    scaleExtent: vi.fn(() => ({
      on: vi.fn()
    })),
    scaleBy: vi.fn(),
    transform: vi.fn()
  })),
  zoomIdentity: {
    translate: vi.fn(() => ({
      scale: vi.fn()
    }))
  }
}));

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// Mock DOMParser
global.DOMParser = vi.fn(() => ({
  parseFromString: vi.fn((xmlString) => {
    if (xmlString.includes('unclosed')) {
      return {
        getElementsByTagName: vi.fn(() => [{ textContent: 'Parse error' }]),
        documentElement: null
      };
    }
    return {
      getElementsByTagName: vi.fn(() => []),
      documentElement: {
        nodeName: 'root',
        attributes: [],
        childNodes: [
          {
            nodeType: 1,
            nodeName: 'message',
            attributes: [{ name: 'type', value: 'MT103' }],
            childNodes: []
          }
        ]
      }
    };
  })
}));

// Mock console methods to reduce noise in tests
global.console = {
  ...console,
  warn: vi.fn(),
  error: vi.fn(),
  log: vi.fn()
};
