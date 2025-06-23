import React from 'react';
import { Snackbar, Alert } from '@mui/material';

const Notification = ({ open = false, handleClose, message = '', severity = 'info' }) => {
  if (!open || !message) {
    return null;
  }

  return (
    <Snackbar
      open={open}
      autoHideDuration={6000}
      onClose={handleClose}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      <Alert
        onClose={handleClose}
        severity={severity || 'info'}
        sx={{ width: '100%' }}
      >
        {message}
      </Alert>
    </Snackbar>
  );
};

export default Notification; 