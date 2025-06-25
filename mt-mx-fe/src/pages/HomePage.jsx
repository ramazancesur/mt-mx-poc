import React, { useState, useEffect } from 'react';
import {
    Box,
    Typography,
    Paper,
    Grid,
    Card,
    CardContent,
    CardActions,
    Button,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Alert,
    CircularProgress,
    Divider,
    Chip,
    IconButton,
    Tooltip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TablePagination
} from '@mui/material';
import {
    CloudUpload as UploadIcon,
    Send as SendIcon,
    Description as FileIcon,
    CheckCircle as ValidIcon,
    Error as ErrorIcon,
    Info as InfoIcon,
    Refresh as RefreshIcon,
    Visibility as ViewIcon
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { useMessages } from '../context/MessageContext';
import SwiftMessageService from '../services/swiftMessageService';

const HomePage = () => {
    const { t } = useTranslation();
    const { setNotification } = useMessages();

    // Form state
    const [messageType, setMessageType] = useState('MT103');
    const [senderBic, setSenderBic] = useState('');
    const [receiverBic, setReceiverBic] = useState('');
    const [amount, setAmount] = useState('');
    const [currency, setCurrency] = useState('EUR');
    const [valueDate, setValueDate] = useState('');
    const [rawMessage, setRawMessage] = useState('');

    // File upload state
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileValidation, setFileValidation] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [sending, setSending] = useState(false);

    // Messages list state
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [totalElements, setTotalElements] = useState(0);

    // Validation state
    const [errors, setErrors] = useState({});

    // Load messages on component mount
    useEffect(() => {
        loadMessages();
    }, [page, rowsPerPage]);

    const loadMessages = async () => {
        setLoading(true);
        try {
            console.log('Loading messages with page:', page, 'rowsPerPage:', rowsPerPage);
            const response = await SwiftMessageService.getMessages(page, rowsPerPage);
            console.log('Response received:', response);

            if (response && response.success) {
                const content = response.data?.content || [];
                const totalElements = response.data?.totalElements || 0;
                console.log('Setting messages:', content, 'totalElements:', totalElements);
                setMessages(content);
                setTotalElements(totalElements);
            } else {
                console.error('Response not successful:', response);
                setMessages([]);
                setTotalElements(0);
                setNotification({
                    open: true,
                    message: response?.message || 'Failed to load messages',
                    severity: 'error'
                });
            }
        } catch (error) {
            console.error('Error loading messages:', error);
            setMessages([]);
            setTotalElements(0);
            setNotification({
                open: true,
                message: 'Backend bağlantısı kurulamadı. Backend servisinin çalıştığından emin olun.',
                severity: 'error'
            });
        } finally {
            setLoading(false);
        }
    };

    const messageTypes = [
        { value: 'MT102', label: 'MT102 - Multiple Customer Credit Transfer' },
        { value: 'MT103', label: 'MT103 - Single Customer Credit Transfer' },
        { value: 'MT202', label: 'MT202 - General Financial Institution Transfer' },
        { value: 'MT202COV', label: 'MT202COV - General Financial Institution Transfer for Cover' },
        { value: 'MT203', label: 'MT203 - Multiple General Financial Institution Transfer' }
    ];

    const currencies = ['EUR', 'USD', 'GBP', 'TRY', 'CHF'];

    const validateForm = () => {
        const newErrors = {};

        if (!senderBic.trim()) {
            newErrors.senderBic = 'Sender BIC is required';
        } else if (senderBic.length !== 11) {
            newErrors.senderBic = 'BIC must be 11 characters';
        }

        if (!receiverBic.trim()) {
            newErrors.receiverBic = 'Receiver BIC is required';
        } else if (receiverBic.length !== 11) {
            newErrors.receiverBic = 'BIC must be 11 characters';
        }

        if (!amount || parseFloat(amount) <= 0) {
            newErrors.amount = 'Valid amount is required';
        }

        if (!valueDate) {
            newErrors.valueDate = 'Value date is required';
        }

        if (!rawMessage.trim()) {
            newErrors.rawMessage = 'Raw MT message is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const validateFile = (file) => {
        if (!file) return { valid: false, message: 'No file selected' };

        const allowedTypes = ['text/plain', 'application/octet-stream'];
        const maxSize = 1024 * 1024; // 1MB

        if (!allowedTypes.includes(file.type) && !file.name.endsWith('.txt')) {
            return { valid: false, message: 'Only .txt files are allowed' };
        }

        if (file.size > maxSize) {
            return { valid: false, message: 'File size must be less than 1MB' };
        }

        return { valid: true, message: 'File is valid' };
    };

    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        setSelectedFile(file);
        setFileValidation(validateFile(file));
    };

    const handleFileUpload = async () => {
        if (!selectedFile || !fileValidation?.valid) {
            setNotification({
                open: true,
                message: 'Please select a valid file first',
                severity: 'error'
            });
            return;
        }

        setUploading(true);
        try {
            const formData = new FormData();
            formData.append('file', selectedFile);
            formData.append('messageType', messageType);

            const response = await SwiftMessageService.uploadMessageFile(formData);

            setNotification({
                open: true,
                message: 'File uploaded successfully!',
                severity: 'success'
            });

            setSelectedFile(null);
            setFileValidation(null);

        } catch (error) {
            console.error('File upload error:', error);
            setNotification({
                open: true,
                message: error.response?.data?.message || 'File upload failed',
                severity: 'error'
            });
        } finally {
            setUploading(false);
        }
    };

    const handleManualSubmit = async () => {
        if (!validateForm()) {
            setNotification({
                open: true,
                message: 'Please fix the validation errors',
                severity: 'error'
            });
            return;
        }

        setSending(true);
        try {
            const messageData = {
                messageType,
                senderBic: senderBic.trim(),
                receiverBic: receiverBic.trim(),
                amount: parseFloat(amount),
                currency,
                valueDate,
                rawMtMessage: rawMessage.trim()
            };

            const response = await SwiftMessageService.createMessage(messageData);

            setNotification({
                open: true,
                message: 'Message created successfully!',
                severity: 'success'
            });

            // Reset form
            setSenderBic('');
            setReceiverBic('');
            setAmount('');
            setValueDate('');
            setRawMessage('');
            setErrors({});

        } catch (error) {
            console.error('Message creation error:', error);
            setNotification({
                open: true,
                message: error.response?.data?.message || 'Message creation failed',
                severity: 'error'
            });
        } finally {
            setSending(false);
        }
    };

    const generateSampleMessage = () => {
        const samples = {
            MT103: `{1:F01TGBATRIXXX0000000000}{2:I103DEUTDEFFXXXXN}{4:
:20:MT103REF001
:23B:CRED
:32A:250625EUR25000,00
:50K:/TR330006200119000006672315
AHMET YILMAZ
ATATURK CAD. NO:123
KADIKOY/ISTANBUL
:52A:TGBATRIXXX
:57A:DEUTDEFFXXX
:59:/DE89370400440532013000
MUELLER GMBH
HAUPTSTRASSE 45
FRANKFURT/GERMANY
:70:INVOICE PAYMENT INV-2024-001
:71A:SHA
-}`,
            MT102: `{1:F01TGBATRIXXX0000000000}{2:I102AKBKTRISKXXXXN}{4:
:20:MT102BATCH001
:21:SAL202412001
:32A:250625TRY45000,00
:50A:TGBATRIXXX
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/ACC/SALARY PAYMENTS DECEMBER 2024
:21:EMP001
:32B:TRY15000,00
:50K:/TR330006200000000001234567
AHMET YILMAZ
PERSONEL DEPARTMANI
:59:/TR640001300000000001111111
AHMET YILMAZ
KADIKOY ISTANBUL
:70:DECEMBER 2024 SALARY
-}`,
            MT202: `{1:F01TGBATRIXXX0000000000}{2:I202DEUTDEFFXXXXN}{4:
:20:MT202REF001
:21:SETTLE20241201
:32A:250625EUR500000,00
:52A:TGBATRIXXX
:53A:BANKDEFM
:54A:DEUTSCHEBANK
:57A:DEUTDEFFXXX
:58A:COMMERZBANK
:72:/INS/DAILY SETTLEMENT EUR
-}`,
            MT202COV: `{1:F01TGBATRIXXX0000000000}{2:I202CHASUS33XXXXN}{4:
:20:MT202COVREF001
:21:COV20241201001
:32A:250625USD100000,00
:52A:TGBATRIXXX
:53A:CITITRIS
:57A:CHASUS33XXX
:58A:CHASUS33XXX
:50A:TGBATRIXXX
:59A:BENEFICIARY_BANK
:70:COVER FOR MT103 PAYMENT
:72:/INS/USD COVER PAYMENT
-}`,
            MT203: `{1:F01TGBATRIXXX0000000000}{2:I203AKBKTRISKXXXXN}{4:
:20:MT203BATCH001
:21:MULTI20241201
:32A:250625TRY1500000,00
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/INS/MULTIPLE SETTLEMENTS
:21:SET001
:32B:TRY500000,00
:56A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/INS/SETTLEMENT 1
:21:SET002
:32B:TRY500000,00
:56A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/INS/SETTLEMENT 2
:21:SET003
:32B:TRY500000,00
:56A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/INS/SETTLEMENT 3
-}`
        };

        setRawMessage(samples[messageType] || '');
    };

    return (
        <Box>
            <Typography variant="h4" component="h1" gutterBottom>
                SWIFT MT-MX Message Converter
            </Typography>

            <Typography variant="body1" color="text.secondary" paragraph>
                Create new SWIFT messages manually or upload from file. All messages will be validated and converted to MX format.
            </Typography>

            <Grid container spacing={3}>
                {/* Manual Message Creation */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Manual Message Creation
                            </Typography>

                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <FormControl fullWidth>
                                        <InputLabel>Message Type</InputLabel>
                                        <Select
                                            value={messageType}
                                            label="Message Type"
                                            onChange={(e) => setMessageType(e.target.value)}
                                        >
                                            {messageTypes.map((type) => (
                                                <MenuItem key={type.value} value={type.value}>
                                                    {type.label}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        label="Sender BIC"
                                        value={senderBic}
                                        onChange={(e) => setSenderBic(e.target.value)}
                                        error={!!errors.senderBic}
                                        helperText={errors.senderBic}
                                        placeholder="TGBATRIXXX"
                                    />
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        label="Receiver BIC"
                                        value={receiverBic}
                                        onChange={(e) => setReceiverBic(e.target.value)}
                                        error={!!errors.receiverBic}
                                        helperText={errors.receiverBic}
                                        placeholder="DEUTDEFFXXX"
                                    />
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        fullWidth
                                        label="Amount"
                                        type="number"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        error={!!errors.amount}
                                        helperText={errors.amount}
                                    />
                                </Grid>

                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Currency</InputLabel>
                                        <Select
                                            value={currency}
                                            label="Currency"
                                            onChange={(e) => setCurrency(e.target.value)}
                                        >
                                            {currencies.map((curr) => (
                                                <MenuItem key={curr} value={curr}>{curr}</MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Value Date"
                                        type="date"
                                        value={valueDate}
                                        onChange={(e) => setValueDate(e.target.value)}
                                        error={!!errors.valueDate}
                                        helperText={errors.valueDate}
                                        InputLabelProps={{ shrink: true }}
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Raw MT Message"
                                        multiline
                                        rows={8}
                                        value={rawMessage}
                                        onChange={(e) => setRawMessage(e.target.value)}
                                        error={!!errors.rawMessage}
                                        helperText={errors.rawMessage}
                                        placeholder="Enter SWIFT MT message content..."
                                    />
                                </Grid>
                            </Grid>
                        </CardContent>

                        <CardActions>
                            <Button
                                variant="outlined"
                                onClick={generateSampleMessage}
                                startIcon={<InfoIcon />}
                            >
                                Generate Sample
                            </Button>
                            <Button
                                variant="contained"
                                onClick={handleManualSubmit}
                                disabled={sending}
                                startIcon={sending ? <CircularProgress size={20} /> : <SendIcon />}
                            >
                                {sending ? 'Creating...' : 'Create Message'}
                            </Button>
                        </CardActions>
                    </Card>
                </Grid>

                {/* File Upload */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                File Upload
                            </Typography>

                            <Box sx={{ mb: 2 }}>
                                <input
                                    accept=".txt"
                                    style={{ display: 'none' }}
                                    id="file-upload"
                                    type="file"
                                    onChange={handleFileSelect}
                                />
                                <label htmlFor="file-upload">
                                    <Button
                                        variant="outlined"
                                        component="span"
                                        startIcon={<UploadIcon />}
                                        fullWidth
                                        sx={{ mb: 2 }}
                                    >
                                        Select File (.txt)
                                    </Button>
                                </label>

                                {selectedFile && (
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                                        <FileIcon />
                                        <Typography variant="body2">
                                            {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)
                                        </Typography>
                                        {fileValidation?.valid ? (
                                            <ValidIcon color="success" />
                                        ) : (
                                            <ErrorIcon color="error" />
                                        )}
                                    </Box>
                                )}

                                {fileValidation && (
                                    <Alert
                                        severity={fileValidation.valid ? 'success' : 'error'}
                                        sx={{ mb: 2 }}
                                    >
                                        {fileValidation.message}
                                    </Alert>
                                )}
                            </Box>

                            <FormControl fullWidth sx={{ mb: 2 }}>
                                <InputLabel>Message Type</InputLabel>
                                <Select
                                    value={messageType}
                                    label="Message Type"
                                    onChange={(e) => setMessageType(e.target.value)}
                                >
                                    {messageTypes.map((type) => (
                                        <MenuItem key={type.value} value={type.value}>
                                            {type.label}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </CardContent>

                        <CardActions>
                            <Button
                                variant="contained"
                                onClick={handleFileUpload}
                                disabled={!selectedFile || !fileValidation?.valid || uploading}
                                startIcon={uploading ? <CircularProgress size={20} /> : <UploadIcon />}
                                fullWidth
                            >
                                {uploading ? 'Uploading...' : 'Upload File'}
                            </Button>
                        </CardActions>
                    </Card>

                    {/* File Format Info */}
                    <Card sx={{ mt: 2 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                File Format Requirements
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                • Only .txt files are accepted<br />
                                • Maximum file size: 1MB<br />
                                • File should contain valid SWIFT MT message format<br />
                                • Message type will be auto-detected or specified manually
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Messages List */}
            <Box sx={{ mt: 4 }}>
                <Card>
                    <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                            <Typography variant="h6">
                                Recent Messages
                            </Typography>
                            <Button
                                variant="outlined"
                                startIcon={<RefreshIcon />}
                                onClick={loadMessages}
                                disabled={loading}
                            >
                                Refresh
                            </Button>
                        </Box>

                        {loading ? (
                            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                                <CircularProgress />
                            </Box>
                        ) : messages.length === 0 ? (
                            <Alert severity="info">
                                No messages found. Create a new message to get started.
                            </Alert>
                        ) : (
                            <>
                                <TableContainer component={Paper} variant="outlined">
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>ID</TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell>Sender BIC</TableCell>
                                                <TableCell>Receiver BIC</TableCell>
                                                <TableCell>Amount</TableCell>
                                                <TableCell>Currency</TableCell>
                                                <TableCell>Value Date</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {messages.map((message) => (
                                                <TableRow key={message.id}>
                                                    <TableCell>{message.id}</TableCell>
                                                    <TableCell>
                                                        <Chip
                                                            label={message.messageType}
                                                            size="small"
                                                            color="primary"
                                                        />
                                                    </TableCell>
                                                    <TableCell>{message.senderBic}</TableCell>
                                                    <TableCell>{message.receiverBic}</TableCell>
                                                    <TableCell>{message.amount}</TableCell>
                                                    <TableCell>{message.currency}</TableCell>
                                                    <TableCell>{message.valueDate}</TableCell>
                                                    <TableCell>
                                                        <Chip
                                                            label={message.status || 'PENDING'}
                                                            size="small"
                                                            color={message.status === 'CONVERTED' ? 'success' : 'warning'}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <Tooltip title="View Details">
                                                            <IconButton size="small">
                                                                <ViewIcon />
                                                            </IconButton>
                                                        </Tooltip>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </TableContainer>

                                <TablePagination
                                    component="div"
                                    count={totalElements}
                                    page={page}
                                    onPageChange={(event, newPage) => setPage(newPage)}
                                    rowsPerPage={rowsPerPage}
                                    onRowsPerPageChange={(event) => {
                                        setRowsPerPage(parseInt(event.target.value, 10));
                                        setPage(0);
                                    }}
                                    rowsPerPageOptions={[5, 10, 25, 50]}
                                />
                            </>
                        )}
                    </CardContent>
                </Card>
            </Box>
        </Box>
    );
};

export default HomePage; 