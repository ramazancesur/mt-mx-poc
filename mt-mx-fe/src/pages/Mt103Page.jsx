import React, { useEffect, useState } from 'react';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress, Alert, Button, IconButton, Pagination, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useTranslation } from 'react-i18next';
import { useMessages } from '../context/MessageContext';
import MessageForm from '../components/MessageForm';
import MessageDetail from '../components/MessageDetail';
import SwiftMessageService from '../services/swiftMessageService';

const Mt103Page = () => {
  const { t } = useTranslation();
  const { messagePage, loading, fetchMessagesByType, removeMessage } = useMessages();
  const [formOpen, setFormOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  useEffect(() => {
    if (fetchMessagesByType) {
      fetchMessagesByType('MT103', page - 1, pageSize);
    }
  }, [fetchMessagesByType, page, pageSize]);

  const handleOpenForm = () => setFormOpen(true);
  const handleCloseForm = () => setFormOpen(false);

  const handleOpenDetail = async (message) => {
    if (!message?.id) {
      console.error('Invalid message object:', message);
      return;
    }

    try {
      // Backend'den güncel mesajı al (otomatik dönüşüm ile)
      const response = await SwiftMessageService.getMessageById(message.id);
      setSelectedMessage(response.data || response);
      setDetailOpen(true);
    } catch (error) {
      console.error('Mesaj detayı alınırken hata:', error);
      // Hata durumunda mevcut mesajı kullan
      setSelectedMessage(message);
      setDetailOpen(true);
    }
  };

  const handleCloseDetail = () => {
    setDetailOpen(false);
    setSelectedMessage(null);
  };

  const handleMessageUpdate = (updatedMessage) => {
    setSelectedMessage(updatedMessage);
    // Listeyi yenile
    if (fetchMessagesByType) {
      fetchMessagesByType('MT103', page - 1, pageSize);
    }
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handlePageSizeChange = (event) => {
    setPageSize(event.target.value);
    setPage(1); // Sayfa boyutu değiştiğinde ilk sayfaya dön
  };

  const handleDeleteMessage = async (event, messageId) => {
    event.stopPropagation();
    if (!messageId) {
      console.error('Invalid message ID for deletion');
      return;
    }

    try {
      if (removeMessage) {
        await removeMessage(messageId);
      }
    } catch (error) {
      console.error('Mesaj silinirken hata:', error);
    }
  };

  // Güvenli veri erişimi
  const safeMessagePage = messagePage || { content: [], totalPages: 0, totalElements: 0 };
  const messages = Array.isArray(safeMessagePage.content) ? safeMessagePage.content : [];
  const totalElements = safeMessagePage.totalElements || 0;
  const totalPages = safeMessagePage.totalPages || 1;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" component="h1">
          {t ? t('page_title_mt103') : 'MT103 Messages'}
        </Typography>
        <Button variant="contained" onClick={handleOpenForm}>
          Create New Message
        </Button>
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          MT103 Messages ({totalElements})
        </Typography>
        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Page Size</InputLabel>
          <Select
            value={pageSize}
            label="Page Size"
            onChange={handlePageSizeChange}
          >
            <MenuItem value={5}>5</MenuItem>
            <MenuItem value={10}>10</MenuItem>
            <MenuItem value={20}>20</MenuItem>
            <MenuItem value={50}>50</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <MessageForm open={formOpen} handleClose={handleCloseForm} />
      <MessageDetail message={selectedMessage} open={detailOpen} onClose={handleCloseDetail} onUpdate={handleMessageUpdate} />

      {loading && <CircularProgress />}

      {messages.length === 0 && !loading && (
        <Alert severity="info">No MT103 messages found.</Alert>
      )}

      {messages.length > 0 && !loading && (
        <>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} aria-label="MT103 messages table">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Sender BIC</TableCell>
                  <TableCell>Receiver BIC</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Currency</TableCell>
                  <TableCell>Value Date</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {messages.map((row) => (
                  <TableRow
                    hover
                    onClick={() => handleOpenDetail(row)}
                    key={row.id || Math.random()}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 }, cursor: 'pointer' }}
                  >
                    <TableCell component="th" scope="row">
                      {row.id || 'N/A'}
                    </TableCell>
                    <TableCell>{row.senderBic || 'N/A'}</TableCell>
                    <TableCell>{row.receiverBic || 'N/A'}</TableCell>
                    <TableCell>{row.amount || 'N/A'}</TableCell>
                    <TableCell>{row.currency || 'N/A'}</TableCell>
                    <TableCell>{row.valueDate || 'N/A'}</TableCell>
                    <TableCell>{row.createdAt ? new Date(row.createdAt).toLocaleString() : 'N/A'}</TableCell>
                    <TableCell align="right">
                      <IconButton
                        aria-label="delete"
                        onClick={(e) => handleDeleteMessage(e, row.id)}
                        disabled={!row.id}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <Pagination
              count={Math.max(1, totalPages)}
              page={page}
              onChange={handlePageChange}
              color="primary"
            />
          </Box>
        </>
      )}
    </Box>
  );
};

export default Mt103Page;
