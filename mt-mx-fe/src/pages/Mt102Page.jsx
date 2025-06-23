import React, { useEffect, useState } from 'react';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress, Alert, Button, IconButton, Pagination, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useTranslation } from 'react-i18next';
import { useMessages } from '../context/MessageContext';
import MessageDetail from '../components/MessageDetail';
import SwiftMessageService from '../services/swiftMessageService';

const Mt102Page = () => {
  const { t } = useTranslation();
  const { messagePage, loading, fetchMessagesByType, removeMessage } = useMessages();
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  useEffect(() => {
    fetchMessagesByType('MT102', page - 1, pageSize);
  }, [fetchMessagesByType, page, pageSize]);

  const handleOpenDetail = async (message) => {
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
    fetchMessagesByType('MT102', page - 1, pageSize);
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handlePageSizeChange = (event) => {
    setPageSize(event.target.value);
    setPage(1); // Sayfa boyutu değiştiğinde ilk sayfaya dön
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('page_title_mt102')}
      </Typography>
      <Typography paragraph>
        {t('page_description_mt102')}
      </Typography>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          MT102 Messages ({messagePage.totalElements || 0})
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

      <MessageDetail
        message={selectedMessage}
        open={detailOpen}
        onClose={handleCloseDetail}
        onUpdate={handleMessageUpdate}
      />

      {loading && <CircularProgress />}

      {messagePage.content.length === 0 && !loading && (
        <Alert severity="info">No MT102 messages found.</Alert>
      )}

      {messagePage.content.length > 0 && !loading && (
        <>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} aria-label="MT102 messages table">
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
                {messagePage.content.map((row) => (
                  <TableRow
                    hover
                    onClick={() => handleOpenDetail(row)}
                    key={row.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 }, cursor: 'pointer' }}
                  >
                    <TableCell component="th" scope="row">
                      {row.id}
                    </TableCell>
                    <TableCell>{row.senderBic}</TableCell>
                    <TableCell>{row.receiverBic}</TableCell>
                    <TableCell>{row.amount}</TableCell>
                    <TableCell>{row.currency}</TableCell>
                    <TableCell>{row.valueDate}</TableCell>
                    <TableCell>{new Date(row.createdAt).toLocaleString()}</TableCell>
                    <TableCell align="right">
                      <IconButton aria-label="delete" onClick={(e) => { e.stopPropagation(); removeMessage(row.id); }}>
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
              count={messagePage.totalPages || 1}
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

export default Mt102Page;
