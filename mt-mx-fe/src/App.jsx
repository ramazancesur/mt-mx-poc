import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { I18nextProvider } from 'react-i18next';
import theme from './theme/theme';
import i18n from './i18n';
import { MessageProvider } from './context/MessageContext';
import Layout from './components/Layout';
import Mt102Page from './pages/Mt102Page';
import Mt103Page from './pages/Mt103Page';
import Mt202Page from './pages/Mt202Page';
import Mt203Page from './pages/Mt203Page';

function App() {
  return (
    <I18nextProvider i18n={i18n}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <MessageProvider>
          <Router>
            <Layout>
              <Routes>
                <Route path="/" element={<Navigate to="/mt103" replace />} />
                <Route path="/mt102" element={<Mt102Page />} />
                <Route path="/mt103" element={<Mt103Page />} />
                <Route path="/mt202" element={<Mt202Page />} />
                <Route path="/mt203" element={<Mt203Page />} />
                <Route path="*" element={<Navigate to="/mt103" replace />} />
              </Routes>
            </Layout>
          </Router>
        </MessageProvider>
      </ThemeProvider>
    </I18nextProvider>
  );
}

export default App; 