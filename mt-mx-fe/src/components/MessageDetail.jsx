import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Box,
    Typography,
    TextField,
    Alert,
    Snackbar,
    Paper,
    Divider,
    Tabs,
    Tab
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import SaveIcon from '@mui/icons-material/Save';
import SwiftMessageService from '../services/swiftMessageService';
import D3Tree from './D3Tree';
import ConfirmationDialog from './ConfirmationDialog';
import FileDownloadIcon from '@mui/icons-material/FileDownload';

const MessageDetail = ({ open, onClose, message, onUpdate }) => {
    const [mxContent, setMxContent] = useState('');
    const [originalMxContent, setOriginalMxContent] = useState('');
    const [loading, setLoading] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const [confirmDialog, setConfirmDialog] = useState({ open: false, title: '', content: '', onConfirm: null });
    const [tabIndex, setTabIndex] = useState(0);

    useEffect(() => {
        if (message && open) {
            const mxData = message.generatedMxMessage || '';
            setMxContent(mxData);
            setOriginalMxContent(mxData);
        }
    }, [message, open]);

    const handleTabChange = (event, newValue) => {
        setTabIndex(newValue);
    };

    const handleSaveMx = async () => {
        if (!message || mxContent === originalMxContent) {
            setSnackbar({ open: true, message: 'MX mesajında değişiklik yapılmadı', severity: 'info' });
            return;
        }

        setConfirmDialog({
            open: true,
            title: 'MX Mesajını Güncelle',
            content: 'MX XML içeriğini güncellemek istediğinizden emin misiniz?',
            onConfirm: async () => {
                setLoading(true);
                try {
                    const updatedMessage = await SwiftMessageService.updateXmlContent(message.id, mxContent);
                    setOriginalMxContent(mxContent);
                    if (onUpdate) onUpdate(updatedMessage);
                    setSnackbar({ open: true, message: 'MX XML içeriği başarıyla güncellendi', severity: 'success' });
                } catch (error) {
                    console.error('MX güncelleme hatası:', error);
                    setSnackbar({ open: true, message: 'MX XML güncellenirken hata oluştu', severity: 'error' });
                } finally {
                    setLoading(false);
                }
            }
        });
    };

    const handleDownload = () => {
        const filename = `mx_message_${message?.id || 'unknown'}.xml`;

        if (!mxContent) {
            setSnackbar({ open: true, message: 'İndirilecek MX içeriği yok', severity: 'warning' });
            return;
        }

        const blob = new Blob([mxContent], { type: 'application/xml' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        setSnackbar({ open: true, message: 'MX XML dosyası indirildi', severity: 'success' });
    };

    const handleDownloadMt = () => {
        const filename = `mt_message_${message?.id || 'unknown'}.txt`;
        const mtContent = message?.rawMtMessage || '';
        if (!mtContent) {
            setSnackbar({ open: true, message: 'İndirilecek MT içeriği yok', severity: 'warning' });
            return;
        }
        const blob = new Blob([mtContent], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        setSnackbar({ open: true, message: 'MT mesajı indirildi', severity: 'success' });
    };

    const hasMxChanges = mxContent !== originalMxContent;

    return (
        <>
            <Dialog
                open={open}
                onClose={onClose}
                maxWidth="xl"
                fullWidth
                PaperProps={{
                    sx: { height: '95vh', display: 'flex', flexDirection: 'column' }
                }}
            >
                <DialogTitle>
                    <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Typography variant="h5" sx={{ fontWeight: 600, color: 'primary.main' }}>
                            Mesaj Detayı - {message?.messageType}
                        </Typography>
                        <Typography variant="subtitle1" sx={{ color: 'text.secondary' }}>
                            ID: {message?.id}
                        </Typography>
                    </Box>
                </DialogTitle>
                <Tabs value={tabIndex} onChange={handleTabChange} sx={{ px: 3, pt: 1 }}>
                    <Tab label="MT" />
                    <Tab label="MX" />
                </Tabs>
                <DialogContent sx={{ flex: 1, p: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
                    {tabIndex === 0 && (
                        <Paper elevation={2} sx={{ p: 3, display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <Box display="flex" justifyContent="space-between" alignItems="center">
                                <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main' }}>
                                    MT Mesajı
                                </Typography>
                                <Button
                                    startIcon={<FileDownloadIcon />}
                                    onClick={handleDownloadMt}
                                    disabled={!message?.rawMtMessage}
                                    variant="outlined"
                                    size="small"
                                >
                                    MT Mesajını İndir
                                </Button>
                            </Box>
                            <TextField
                                multiline
                                fullWidth
                                value={message?.rawMtMessage || ''}
                                InputProps={{ readOnly: true }}
                                minRows={12}
                                maxRows={24}
                                placeholder="MT mesajı burada görünecek..."
                                variant="outlined"
                                sx={{
                                    fontFamily: 'Monaco, Consolas, "Courier New", monospace',
                                    fontSize: '13px',
                                    lineHeight: 1.5
                                }}
                            />
                        </Paper>
                    )}
                    {tabIndex === 1 && (
                        <>
                            <Paper elevation={2} sx={{ p: 3, flex: '0 0 45%', display: 'flex', flexDirection: 'column' }}>
                                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main' }}>
                                        MX XML İçeriği
                                    </Typography>
                                    <Box display="flex" gap={1}>
                                        <Button
                                            startIcon={<DownloadIcon />}
                                            onClick={handleDownload}
                                            disabled={!mxContent}
                                            variant="outlined"
                                            size="small"
                                        >
                                            XML İndir
                                        </Button>
                                        <Button
                                            startIcon={<SaveIcon />}
                                            onClick={handleSaveMx}
                                            disabled={loading || !hasMxChanges}
                                            variant="contained"
                                            color={hasMxChanges ? 'primary' : 'inherit'}
                                            size="small"
                                        >
                                            {loading ? 'Kaydediliyor...' : 'Kaydet'}
                                        </Button>
                                    </Box>
                                </Box>
                                <TextField
                                    multiline
                                    fullWidth
                                    value={mxContent}
                                    onChange={(e) => setMxContent(e.target.value)}
                                    placeholder="MX XML içeriği burada görünecek..."
                                    variant="outlined"
                                    sx={{
                                        flex: 1,
                                        '& .MuiInputBase-root': {
                                            height: '100%',
                                            alignItems: 'flex-start',
                                            fontFamily: 'Monaco, Consolas, "Courier New", monospace',
                                            fontSize: '13px',
                                            lineHeight: 1.5
                                        },
                                        '& .MuiInputBase-input': {
                                            height: '100% !important',
                                            overflow: 'auto !important',
                                            resize: 'none'
                                        }
                                    }}
                                />
                            </Paper>
                            <Divider />
                            <Paper elevation={2} sx={{ p: 3, flex: '1 1 50%', display: 'flex', flexDirection: 'column' }}>
                                <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main', mb: 2 }}>
                                    XML Yapısı Görselleştirmesi
                                </Typography>
                                <Box sx={{ flex: 1, minHeight: '400px', border: '1px solid #e0e0e0', borderRadius: 1 }}>
                                    <D3Tree
                                        key={message?.id || 'empty'}
                                        xmlData={mxContent}
                                        onXmlUpdate={setMxContent}
                                    />
                                </Box>
                            </Paper>
                        </>
                    )}
                </DialogContent>
                <DialogActions sx={{ p: 3, borderTop: '1px solid #e0e0e0' }}>
                    <Button
                        onClick={onClose}
                        variant="outlined"
                        size="large"
                    >
                        Kapat
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Confirmation Dialog */}
            <ConfirmationDialog
                open={confirmDialog.open}
                title={confirmDialog.title}
                content={confirmDialog.content}
                onConfirm={() => {
                    if (confirmDialog.onConfirm) confirmDialog.onConfirm();
                    setConfirmDialog({ open: false, title: '', content: '', onConfirm: null });
                }}
                onCancel={() => setConfirmDialog({ open: false, title: '', content: '', onConfirm: null })}
            />

            {/* Snackbar for notifications */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={() => setSnackbar({ ...snackbar, open: false })}
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            >
                <Alert
                    onClose={() => setSnackbar({ ...snackbar, open: false })}
                    severity={snackbar.severity || 'info'}
                    variant="filled"
                >
                    {snackbar.message || ''}
                </Alert>
            </Snackbar>
        </>
    );
};

export default MessageDetail; 