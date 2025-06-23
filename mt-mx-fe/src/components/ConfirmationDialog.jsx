import React from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Typography,
    Box,
    Alert
} from '@mui/material';
import {
    Warning as WarningIcon,
    Delete as DeleteIcon,
    Edit as EditIcon,
    SwapHoriz as ConvertIcon
} from '@mui/icons-material';

const ConfirmationDialog = ({
    open,
    onClose,
    onConfirm,
    title,
    message,
    type = 'warning', // 'warning', 'danger', 'info'
    confirmText = 'Evet',
    cancelText = 'İptal',
    loading = false
}) => {
    const getIcon = () => {
        switch (type) {
            case 'delete':
                return <DeleteIcon sx={{ color: 'error.main', fontSize: 40 }} />;
            case 'edit':
                return <EditIcon sx={{ color: 'warning.main', fontSize: 40 }} />;
            case 'convert':
                return <ConvertIcon sx={{ color: 'info.main', fontSize: 40 }} />;
            default:
                return <WarningIcon sx={{ color: 'warning.main', fontSize: 40 }} />;
        }
    };

    const getAlertSeverity = () => {
        switch (type) {
            case 'delete':
                return 'error';
            case 'edit':
                return 'warning';
            case 'convert':
                return 'info';
            default:
                return 'warning';
        }
    };

    const getConfirmButtonColor = () => {
        switch (type) {
            case 'delete':
                return 'error';
            case 'edit':
                return 'warning';
            case 'convert':
                return 'primary';
            default:
                return 'primary';
        }
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="sm"
            fullWidth
            PaperProps={{
                sx: { borderRadius: 2 }
            }}
        >
            <DialogTitle sx={{ pb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    {getIcon()}
                    <Typography variant="h6" component="div">
                        {title}
                    </Typography>
                </Box>
            </DialogTitle>
            
            <DialogContent>
                <Alert severity={getAlertSeverity()} sx={{ mb: 2 }}>
                    <Typography variant="body1">
                        {message}
                    </Typography>
                </Alert>
                
                {type === 'delete' && (
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        ⚠️ Bu işlem geri alınamaz!
                    </Typography>
                )}
            </DialogContent>
            
            <DialogActions sx={{ p: 3, pt: 1 }}>
                <Button
                    onClick={onClose}
                    variant="outlined"
                    disabled={loading}
                    sx={{ minWidth: 100 }}
                >
                    {cancelText}
                </Button>
                <Button
                    onClick={onConfirm}
                    variant="contained"
                    color={getConfirmButtonColor()}
                    disabled={loading}
                    sx={{ minWidth: 100 }}
                >
                    {loading ? 'İşleniyor...' : confirmText}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfirmationDialog; 