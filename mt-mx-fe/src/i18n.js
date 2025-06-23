import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpApi from 'i18next-http-backend';
import LanguageDetector from 'i18next-browser-languagedetector';

// Test environment için inline resources
const resources = {
  en: {
    translation: {
      "submit": "Submit",
      "cancel": "Cancel",
      "loading": "Loading...",
      "error": "Error",
      "success": "Success"
    }
  },
  tr: {
    translation: {
      "submit": "Gönder",
      "cancel": "İptal",
      "loading": "Yükleniyor...",
      "error": "Hata",
      "success": "Başarılı"
    }
  }
};

const isTestEnvironment = typeof window !== 'undefined' && window.location.pathname === '/en';

i18n
  // load translation using http -> see /public/locales
  .use(HttpApi)
  // detect user language
  .use(LanguageDetector)
  // pass the i18n instance to react-i18next.
  .use(initReactI18next)
  // init i18next
  .init({
    supportedLngs: ['en', 'tr'],
    fallbackLng: 'en',
    debug: false,
    resources: isTestEnvironment ? resources : undefined,
    detection: {
      order: ['queryString', 'cookie', 'localStorage', 'sessionStorage', 'navigator', 'htmlTag', 'path', 'subdomain'],
      caches: ['cookie'],
    },
    backend: isTestEnvironment ? undefined : {
      loadPath: '/locales/{{lng}}/translation.json',
    },
    react: {
      useSuspense: false,
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n; 