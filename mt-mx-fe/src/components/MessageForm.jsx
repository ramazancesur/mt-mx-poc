import React, { useState } from 'react';
import { 
    Button, 
    Dialog, 
    DialogActions, 
    DialogContent, 
    DialogContentText, 
    DialogTitle, 
    TextField, 
    MenuItem, 
    Box, 
    Typography, 
    Alert,
    Divider,
    FormControl,
    InputLabel,
    Select
} from '@mui/material';
import { useMessages } from '../context/MessageContext';

const MessageForm = ({ open, handleClose }) => {
    const { addMessage } = useMessages();
    const [formData, setFormData] = useState({
        messageType: 'MT103',
        senderBic: '',
        receiverBic: '',
        amount: '',
        currency: 'USD',
        valueDate: new Date().toISOString().split('T')[0]
    });
    
    // MT/MX yükleme için yeni state'ler
    const [uploadMode, setUploadMode] = useState('manual'); // 'manual' veya 'upload'
    const [uploadedMessage, setUploadedMessage] = useState('');
    const [uploadedMessageType, setUploadedMessageType] = useState('MT103');
    const [validationError, setValidationError] = useState('');
    const [validationSuccess, setValidationSuccess] = useState('');

    const handleChange = (event) => {
        setFormData({
            ...formData,
            [event.target.name]: event.target.value,
        });
    };

    const handleSubmit = () => {
        if (uploadMode === 'manual') {
            addMessage(formData);
        } else {
            // Uploaded message'ı işle
            if (!uploadedMessage.trim()) {
                setValidationError('Lütfen bir mesaj yükleyin');
                return;
            }
            
            const messageData = {
                messageType: uploadedMessageType,
                rawMtMessage: uploadedMessage,
                senderBic: extractBIC(uploadedMessage, 'sender'),
                receiverBic: extractBIC(uploadedMessage, 'receiver'),
                amount: extractAmount(uploadedMessage),
                currency: extractCurrency(uploadedMessage),
                valueDate: extractValueDate(uploadedMessage)
            };
            
            addMessage(messageData);
        }
        handleClose();
        resetForm();
    };

    const resetForm = () => {
        setFormData({
            messageType: 'MT103',
            senderBic: '',
            receiverBic: '',
            amount: '',
            currency: 'USD',
            valueDate: new Date().toISOString().split('T')[0]
        });
        setUploadedMessage('');
        setUploadedMessageType('MT103');
        setValidationError('');
        setValidationSuccess('');
    };

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const content = e.target.result;
                setUploadedMessage(content);
                validateMessage(content, uploadedMessageType);
            };
            reader.readAsText(file);
        }
    };

    const validateMessage = (message, type) => {
        setValidationError('');
        setValidationSuccess('');
        
        if (!message.trim()) {
            setValidationError('Mesaj boş olamaz');
            return;
        }

        if (type.startsWith('MT')) {
            // MT mesaj validation
            if (!message.includes('{1:') || !message.includes('{2:') || !message.includes('{4:')) {
                setValidationError('Geçersiz MT mesaj formatı. MT mesajları {1:}, {2:}, {4:} bloklarını içermelidir.');
                return;
            }
            
            // MT tip kontrolü
            const expectedType = type.substring(2); // MT103 -> 103
            if (!message.includes(`{2:I${expectedType}`)) {
                setValidationError(`Mesaj içeriği ${type} formatına uygun değil`);
                return;
            }
            
            setValidationSuccess(`${type} mesajı geçerli format`);
        } else if (type.startsWith('MX')) {
            // MX mesaj validation (XML)
            if (!message.trim().startsWith('<?xml') && !message.trim().startsWith('<')) {
                setValidationError('Geçersiz MX mesaj formatı. MX mesajları XML formatında olmalıdır.');
                return;
            }
            
            try {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(message, "text/xml");
                const parseError = xmlDoc.getElementsByTagName('parsererror');
                if (parseError.length > 0) {
                    setValidationError('Geçersiz XML formatı');
                    return;
                }
                setValidationSuccess(`${type} mesajı geçerli XML format`);
            } catch (error) {
                setValidationError('XML parse hatası');
            }
        }
    };

    const extractBIC = (message, type) => {
        if (type === 'sender') {
            const match = message.match(/\{1:F01([A-Z]{6}[A-Z0-9]{2}[A-Z0-9]{3})/);
            return match ? match[1] : 'UNKNOWN';
        } else {
            const match = message.match(/\{2:I[0-9]{3}([A-Z]{6}[A-Z0-9]{2}[A-Z0-9]{3})/);
            return match ? match[1] : 'UNKNOWN';
        }
    };

    const extractAmount = (message) => {
        const match = message.match(/:32A:[0-9]{6}[A-Z]{3}([0-9,]+)/);
        if (match) {
            return parseFloat(match[1].replace(',', '.'));
        }
        return 1000.00;
    };

    const extractCurrency = (message) => {
        const match = message.match(/:32A:[0-9]{6}([A-Z]{3})[0-9,]+/);
        return match ? match[1] : 'USD';
    };

    const extractValueDate = (message) => {
        const match = message.match(/:32A:([0-9]{6})[A-Z]{3}[0-9,]+/);
        if (match) {
            const dateStr = match[1];
            const year = 2000 + parseInt(dateStr.substring(0, 2));
            const month = parseInt(dateStr.substring(2, 4));
            const day = parseInt(dateStr.substring(4, 6));
            return new Date(year, month - 1, day).toISOString().split('T')[0];
        }
        return new Date().toISOString().split('T')[0];
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
            <DialogTitle>SWIFT Mesaj Oluştur</DialogTitle>
            <DialogContent>
                <Box sx={{ mb: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        Mesaj Oluşturma Modu
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                        <Button 
                            variant={uploadMode === 'manual' ? 'contained' : 'outlined'}
                            onClick={() => setUploadMode('manual')}
                        >
                            Manuel Giriş
                        </Button>
                        <Button 
                            variant={uploadMode === 'upload' ? 'contained' : 'outlined'}
                            onClick={() => setUploadMode('upload')}
                        >
                            MT/MX Mesaj Yükle
                        </Button>
                    </Box>
                </Box>

                <Divider sx={{ mb: 2 }} />

                {uploadMode === 'manual' ? (
                    // Manuel giriş formu
                    <Box>
                        <DialogContentText sx={{ mb: 2 }}>
                            Mesaj detaylarını manuel olarak girin.
                        </DialogContentText>
                        <TextField select label="Message Type" name="messageType" value={formData.messageType} onChange={handleChange} fullWidth margin="dense">
                            <MenuItem value="MT102">MT102</MenuItem>
                            <MenuItem value="MT103">MT103</MenuItem>
                            <MenuItem value="MT202">MT202</MenuItem>
                            <MenuItem value="MT203">MT203</MenuItem>
                        </TextField>
                        <TextField label="Sender BIC" name="senderBic" value={formData.senderBic} onChange={handleChange} fullWidth margin="dense" />
                        <TextField label="Receiver BIC" name="receiverBic" value={formData.receiverBic} onChange={handleChange} fullWidth margin="dense" />
                        <TextField label="Amount" name="amount" type="number" value={formData.amount} onChange={handleChange} fullWidth margin="dense" />
                        <TextField label="Currency" name="currency" value={formData.currency} onChange={handleChange} fullWidth margin="dense" />
                        <TextField label="Value Date" name="valueDate" type="date" value={formData.valueDate} onChange={handleChange} fullWidth margin="dense" InputLabelProps={{ shrink: true }} />
                    </Box>
                ) : (
                    // MT/MX yükleme formu
                    <Box>
                        <DialogContentText sx={{ mb: 2 }}>
                            MT veya MX mesajını yükleyin veya yapıştırın.
                        </DialogContentText>
                        
                        <FormControl fullWidth margin="dense">
                            <InputLabel>Mesaj Tipi</InputLabel>
                            <Select
                                value={uploadedMessageType}
                                onChange={(e) => {
                                    setUploadedMessageType(e.target.value);
                                    if (uploadedMessage) {
                                        validateMessage(uploadedMessage, e.target.value);
                                    }
                                }}
                            >
                                <MenuItem value="MT102">MT102</MenuItem>
                                <MenuItem value="MT103">MT103</MenuItem>
                                <MenuItem value="MT202">MT202</MenuItem>
                                <MenuItem value="MT203">MT203</MenuItem>
                                <MenuItem value="MX103">MX103 (pacs.008)</MenuItem>
                                <MenuItem value="MX202">MX202 (pacs.009)</MenuItem>
                            </Select>
                        </FormControl>

                        <Box sx={{ mt: 2, mb: 2 }}>
                            <input
                                accept=".txt,.xml"
                                style={{ display: 'none' }}
                                id="message-file-upload"
                                type="file"
                                onChange={handleFileUpload}
                            />
                            <label htmlFor="message-file-upload">
                                <Button variant="outlined" component="span" fullWidth>
                                    Dosya Yükle (.txt, .xml)
                                </Button>
                            </label>
                        </Box>

                        <TextField
                            label="Mesaj İçeriği"
                            multiline
                            rows={8}
                            value={uploadedMessage}
                            onChange={(e) => {
                                setUploadedMessage(e.target.value);
                                validateMessage(e.target.value, uploadedMessageType);
                            }}
                            fullWidth
                            margin="dense"
                            placeholder="MT mesajını buraya yapıştırın veya dosya yükleyin..."
                        />

                        {validationError && (
                            <Alert severity="error" sx={{ mt: 2 }}>
                                {validationError}
                            </Alert>
                        )}

                        {validationSuccess && (
                            <Alert severity="success" sx={{ mt: 2 }}>
                                {validationSuccess}
                            </Alert>
                        )}
                    </Box>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={() => { handleClose(); resetForm(); }}>İptal</Button>
                <Button onClick={handleSubmit} variant="contained">
                    {uploadMode === 'manual' ? 'Oluştur' : 'Yükle'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default MessageForm; 