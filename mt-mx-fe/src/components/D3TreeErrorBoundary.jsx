import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { Error as ErrorIcon } from '@mui/icons-material';

class D3TreeErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('D3Tree Error:', error, errorInfo);
    this.setState({
      error: error,
      errorInfo: errorInfo
    });
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <Box 
          sx={{ 
            p: 3, 
            textAlign: 'center', 
            border: '1px solid #ff9800',
            borderRadius: 1,
            backgroundColor: '#fff3e0'
          }}
        >
          <ErrorIcon sx={{ fontSize: 48, color: '#ff9800', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            XML Görselleştirme Hatası
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            XML verisi görselleştirilirken bir hata oluştu. XML formatının geçerli olduğundan emin olun.
          </Typography>
          <Button 
            variant="outlined" 
            onClick={this.handleRetry}
            sx={{ mr: 1 }}
          >
            Tekrar Dene
          </Button>
          {process.env.NODE_ENV === 'development' && (
            <Box sx={{ mt: 2, textAlign: 'left' }}>
              <Typography variant="caption" component="pre" sx={{ 
                backgroundColor: '#f5f5f5', 
                p: 1, 
                borderRadius: 1,
                fontSize: '0.75rem',
                overflow: 'auto',
                maxHeight: '200px'
              }}>
                {this.state.error && this.state.error.toString()}
                {this.state.errorInfo && this.state.errorInfo.componentStack}
              </Typography>
            </Box>
          )}
        </Box>
      );
    }

    return this.props.children;
  }
}

export default D3TreeErrorBoundary; 