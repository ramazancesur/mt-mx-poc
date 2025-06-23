import React, { useState } from 'react';
import { Box, Typography, Paper, Alert, Accordion, AccordionSummary, AccordionDetails } from '@mui/material';
import { ExpandMore } from '@mui/icons-material';

const D3TreeVisualization = ({ xmlData }) => {
  const [error, setError] = useState(null);

  // XML'i parse et ve ağaç yapısına dönüştür
  const parseXMLToTree = (xmlString) => {
    try {
      if (!xmlString || xmlString.trim() === '') {
        return null;
      }

      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlString, 'text/xml');

      // Parse hatası kontrolü
      const parseError = xmlDoc.getElementsByTagName('parsererror');
      if (parseError.length > 0) {
        throw new Error('XML formatı hatalı');
      }

      const convertElement = (element, level = 0) => {
        const node = {
          name: element.nodeName,
          level: level,
          children: [],
          value: null,
          attributes: []
        };

        // Attribute'ları ekle
        if (element.attributes && element.attributes.length > 0) {
          for (let i = 0; i < element.attributes.length; i++) {
            const attr = element.attributes[i];
            node.attributes.push(`${attr.name}="${attr.value}"`);
          }
        }

        // Text içeriği varsa ekle
        if (element.childNodes.length === 1 && element.childNodes[0].nodeType === 3) {
          const text = element.textContent.trim();
          if (text) {
            node.value = text;
          }
        }

        // Alt elementleri ekle
        Array.from(element.children).forEach(child => {
          const childNode = convertElement(child, level + 1);
          if (childNode) {
            node.children.push(childNode);
          }
        });

        return node;
      };

      return convertElement(xmlDoc.documentElement);
    } catch (err) {
      setError('XML parse hatası: ' + err.message);
      return null;
    }
  };

  // Ağaç node'unu render et
  const renderTreeNode = (node, index = 0) => {
    const hasChildren = node.children && node.children.length > 0;
    const hasValue = node.value && node.value.trim() !== '';
    const hasAttributes = node.attributes && node.attributes.length > 0;

    const nodeColor = hasChildren ? '#4CAF50' : '#2196F3';
    const bgColor = hasChildren ? '#e8f5e8' : '#e3f2fd';

    return (
      <Box key={`${node.name}-${index}`} sx={{ mb: 1 }}>
        <Accordion sx={{ backgroundColor: bgColor, border: `1px solid ${nodeColor}` }}>
          <AccordionSummary
            expandIcon={hasChildren ? <ExpandMore /> : null}
            sx={{
              backgroundColor: nodeColor,
              color: 'white',
              '& .MuiAccordionSummary-content': {
                alignItems: 'center'
              }
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box
                sx={{
                  width: 12,
                  height: 12,
                  borderRadius: '50%',
                  backgroundColor: 'white',
                  border: `2px solid ${nodeColor}`
                }}
              />
              <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                {node.name}
              </Typography>
              {hasValue && (
                <Typography variant="body2" sx={{
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  px: 1,
                  borderRadius: 1,
                  maxWidth: '300px',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap'
                }}>
                  {node.value}
                </Typography>
              )}
            </Box>
          </AccordionSummary>

          {(hasChildren || hasValue || hasAttributes) && (
            <AccordionDetails sx={{ pt: 1 }}>
              {/* Attributes göster */}
              {hasAttributes && (
                <Box sx={{ mb: 2, p: 1, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 'bold' }}>
                    Attributes:
                  </Typography>
                  {node.attributes.map((attr, idx) => (
                    <Typography key={idx} variant="body2" sx={{ fontFamily: 'monospace', color: '#666' }}>
                      {attr}
                    </Typography>
                  ))}
                </Box>
              )}

              {/* Value göster */}
              {hasValue && (
                <Box sx={{ mb: 2, p: 2, backgroundColor: '#fff3e0', borderRadius: 1, border: '1px solid #ffb74d' }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 'bold' }}>
                    İçerik:
                  </Typography>
                  <Typography variant="body2" sx={{
                    fontFamily: 'monospace',
                    wordBreak: 'break-all',
                    backgroundColor: 'white',
                    p: 1,
                    borderRadius: 1,
                    mt: 1
                  }}>
                    {node.value}
                  </Typography>
                </Box>
              )}

              {/* Alt elementleri göster */}
              {hasChildren && (
                <Box sx={{ ml: 2 }}>
                  {node.children.map((child, childIndex) =>
                    renderTreeNode(child, childIndex)
                  )}
                </Box>
              )}
            </AccordionDetails>
          )}
        </Accordion>
      </Box>
    );
  };

  // XML yoksa gösterme
  if (!xmlData || xmlData.trim() === '') {
    return (
      <Box sx={{ p: 3, textAlign: 'center', backgroundColor: '#f5f5f5', borderRadius: 2 }}>
        <Typography variant="h6" color="text.secondary">
          MX XML Verisi Bulunamadı
        </Typography>
        <Typography variant="body2" color="text.secondary">
          XML ağaç görselleştirmesi için önce MT mesajını MX formatına dönüştürün.
        </Typography>
      </Box>
    );
  }

  // Hata varsa göster
  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        <Typography variant="body1">
          {error}
        </Typography>
      </Alert>
    );
  }

  // XML'i parse et
  const treeData = parseXMLToTree(xmlData);

  if (!treeData) {
    return (
      <Alert severity="warning" sx={{ m: 2 }}>
        <Typography variant="body1">
          XML verisi parse edilemedi
        </Typography>
      </Alert>
    );
  }

  return (
    <Box sx={{ width: '100%' }}>
      {/* Başlık */}
      <Paper sx={{ p: 2, mb: 2, backgroundColor: '#e8f5e8' }}>
        <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
          🌳 MX XML Ağaç Yapısı
        </Typography>
        <Typography variant="body2" color="text.secondary">
          XML elementlerini genişletip daraltarak inceleyebilirsiniz
        </Typography>
      </Paper>

      {/* XML Ağaç Görünümü */}
      <Paper sx={{ p: 2, maxHeight: '600px', overflow: 'auto', border: '2px solid #4CAF50' }}>
        {renderTreeNode(treeData)}
      </Paper>

      {/* Bilgi Paneli */}
      <Box sx={{ mt: 2, p: 2, backgroundColor: '#f0f8ff', borderRadius: 2, border: '1px solid #2196F3' }}>
        <Typography variant="body2" sx={{ mb: 1, fontWeight: 'bold', color: '#1976d2' }}>
          📖 Kullanım Kılavuzu:
        </Typography>
        <Typography variant="body2" color="text.secondary">
          🟢 <strong>Yeşil Paneller:</strong> Alt XML elementleri olan node'lar (örn: Document, GrpHdr)
        </Typography>
        <Typography variant="body2" color="text.secondary">
          🔵 <strong>Mavi Paneller:</strong> Text içeriği olan son node'lar (örn: MsgId, Amount)
        </Typography>
        <Typography variant="body2" color="text.secondary">
          📂 <strong>Genişlet/Daralt:</strong> Ok simgesine tıklayarak alt elementleri görüntüleyin
        </Typography>
        <Typography variant="body2" color="text.secondary">
          📄 <strong>İçerik:</strong> Turuncu kutularda XML element değerleri gösterilir
        </Typography>
      </Box>
    </Box>
  );
};

export default D3TreeVisualization; 