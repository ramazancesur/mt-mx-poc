import React, { useEffect, useRef, useState, useCallback } from 'react';
import * as d3 from 'd3';
import {
  Box,
  Button,
  TextField,
  Typography,
  Slider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Paper,
  Tooltip,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Card,
  CardContent,
  Chip,
  Stack
} from '@mui/material';
import {
  ZoomIn,
  ZoomOut,
  CenterFocusStrong,
  SwapHoriz,
  Help,
  Search,
  Save,
  Palette,
  Brightness6,
  Brightness4,
  ExpandMore,
  ExpandLess
} from '@mui/icons-material';
import { useTranslation } from 'react-i18next';

const D3Tree = React.memo(({ xmlData = null, onNodeEdit = () => { }, onXmlUpdate = () => { } }) => {
  const { t } = useTranslation();
  const svgRef = useRef(null);

  // Tüm state'leri component'in en üstünde tanımlıyoruz - hooks rules uyumu için
  const [hasError, setHasError] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [treeData, setTreeData] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedNode, setSelectedNode] = useState(null);
  const [editingNode, setEditingNode] = useState(null);
  const [editValue, setEditValue] = useState('');
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [showSaveConfirmDialog, setShowSaveConfirmDialog] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  const [isVertical, setIsVertical] = useState(true);
  const [nodeSize, setNodeSize] = useState(8);
  const [showHelp, setShowHelp] = useState(false);
  const zoomRef = useRef(null);
  const [expandedNodes, setExpandedNodes] = useState(new Set());
  const [theme, setTheme] = useState('light');
  const [backgroundColor, setBackgroundColor] = useState('#ffffff');
  const [nodeColors, setNodeColors] = useState({
    expanded: '#4caf50',
    collapsed: '#ff9800',
    selected: '#2196f3',
    searchMatch: '#f44336',
    leaf: '#81c784'
  });
  const [linkColor, setLinkColor] = useState('#999999');
  const [textColor, setTextColor] = useState('#333333');
  const [showThemePanel, setShowThemePanel] = useState(false);
  const [modifiedNodes, setModifiedNodes] = useState(new Map());

  // Parse XML to tree structure with enhanced null checks
  const parseXML = useCallback((xmlString) => {
    if (!xmlString || typeof xmlString !== 'string') {
      console.warn('Invalid XML string provided');
      return null;
    }

    try {
      setHasError(false);
      setErrorMessage('');
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlString, 'text/xml');

      if (!xmlDoc || xmlDoc.getElementsByTagName('parsererror').length > 0) {
        console.error('XML parsing error');
        return null;
      }

      const convertNode = (element, path = '', level = 0) => {
        if (!element || !element.nodeName) {
          console.warn('Invalid element found during XML parsing');
          return null;
        }

        const nodeId = `${path}/${element.nodeName}`;
        const children = [];

        // Process child elements with enhanced null checks
        if (element.childNodes && element.childNodes.length > 0) {
          for (let i = 0; i < element.childNodes.length; i++) {
            const child = element.childNodes[i];
            if (child && child.nodeType === Node.ELEMENT_NODE && child.nodeName) {
              const childNode = convertNode(child, nodeId, level + 1);
              if (childNode && childNode.name) {
                children.push(childNode);
              }
            }
          }
        }

        // Get text content with enhanced null checks
        let textContent = '';
        if (element.childNodes && element.childNodes.length === 1 &&
          element.childNodes[0] && element.childNodes[0].nodeType === Node.TEXT_NODE &&
          element.childNodes[0].textContent !== null && element.childNodes[0].textContent !== undefined) {
          textContent = element.childNodes[0].textContent.trim();
        }

        return {
          id: nodeId || `node_${Date.now()}_${Math.random()}`,
          name: element.nodeName || 'unknown',
          value: textContent || '',
          children: children.length > 0 ? children : null,
          isExpanded: level < 2,
          isEditable: textContent && textContent.length > 0,
          level: level || 0,
          path: nodeId || '',
          property: element.nodeName || 'unknown' // Eksik property eklendi
        };
      };

      const rootElement = xmlDoc.documentElement;
      if (!rootElement || !rootElement.nodeName) {
        console.error('No valid root element found in XML');
        return null;
      }

      const tree = convertNode(rootElement);
      if (!tree || !tree.name) {
        console.error('Failed to convert XML to tree structure');
        return null;
      }

      // Update expanded nodes with null checks
      const newExpandedNodes = new Set();
      const collectExpandedNodes = (node) => {
        if (node && node.isExpanded && node.id) {
          newExpandedNodes.add(node.id);
        }
        if (node && node.children && Array.isArray(node.children)) {
          node.children.forEach(child => {
            if (child) {
              collectExpandedNodes(child);
            }
          });
        }
      };
      collectExpandedNodes(tree);
      setExpandedNodes(newExpandedNodes);

      return tree;
    } catch (error) {
      console.error('Error parsing XML:', error);
      setHasError(true);
      setErrorMessage(`XML parsing error: ${error.message || 'Unknown error'}`);
      return null;
    }
  }, []);

  // Reconstruct XML from tree with modifications
  const reconstructXML = useCallback(() => {
    if (!treeData) return null;

    const buildXMLNode = (node) => {
      if (!node || !node.name) {
        console.warn('Invalid node in buildXMLNode:', node);
        return '';
      }

      let xml = `<${node.name}>`;

      if (node.children && Array.isArray(node.children) && node.children.length > 0) {
        node.children.forEach(child => {
          if (child) {
            xml += buildXMLNode(child);
          }
        });
      } else if (node.value || (node.id && modifiedNodes.has(node.id))) {
        const value = (node.id && modifiedNodes.get(node.id)) || node.value || '';
        xml += value;
      }

      xml += `</${node.name}>`;
      return xml;
    };

    const xmlDeclaration = '<?xml version="1.0" encoding="UTF-8"?>\n';
    return xmlDeclaration + buildXMLNode(treeData);
  }, [treeData, modifiedNodes]);

  // Toggle node expand/collapse
  const toggleNode = useCallback((nodeId) => {
    const newExpandedNodes = new Set(expandedNodes);
    if (newExpandedNodes.has(nodeId)) {
      newExpandedNodes.delete(nodeId);
    } else {
      newExpandedNodes.add(nodeId);
    }
    setExpandedNodes(newExpandedNodes);
  }, [expandedNodes]);

  // Update visualization
  const updateVisualization = useCallback(() => {
    if (!treeData) return;

    try {
      const svg = d3.select(svgRef.current);
      if (!svg.node()) return; // SVG ref yoksa çık

      svg.selectAll("*").remove();
      svg.style("background-color", backgroundColor);

      const width = 1200;
      const height = 800;
      const margin = { top: 20, right: 120, bottom: 20, left: 120 };

      svg
        .attr("width", width)
        .attr("height", height);

      const g = svg.append("g")
        .attr("transform", `translate(${margin.left},${margin.top})`);

      // Set up zoom behavior
      const zoomBehavior = d3.zoom()
        .scaleExtent([0.1, 5])
        .on("zoom", (event) => {
          g.attr("transform", event.transform);
        });

      svg.call(zoomBehavior);
      if (zoomBehavior && typeof zoomBehavior.scaleBy === 'function') {
        zoomRef.current = zoomBehavior;
      }

      // Create tree layout
      const tree = d3.tree()
        .size([height - margin.top - margin.bottom, width - margin.left - margin.right]);

      // Convert data to d3 hierarchy with filtered nodes
      const filterExpandedNodes = (node) => {
        if (!node || !node.id) {
          return null;
        }

        const filtered = { ...node };
        if (filtered.children && Array.isArray(filtered.children) && !expandedNodes.has(node.id)) {
          filtered.children = null;
          filtered._children = node.children;
        } else if (filtered.children && Array.isArray(filtered.children)) {
          filtered.children = filtered.children
            .map(filterExpandedNodes)
            .filter(child => child !== null);
        }
        return filtered;
      };

      const filteredData = filterExpandedNodes(treeData);
      if (!filteredData) {
        console.warn('Filtered data is null');
        return;
      }

      const root = d3.hierarchy(filteredData);
      tree(root);

      // Add links
      const link = g.selectAll(".link")
        .data(root.links())
        .enter().append("path")
        .attr("class", "link")
        .style("fill", "none")
        .style("stroke", linkColor)
        .style("stroke-width", "2px")
        .attr("d", d3.linkHorizontal()
          .x(d => d.y)
          .y(d => d.x));

      // Add nodes
      const node = g.selectAll(".node")
        .data(root.descendants())
        .enter().append("g")
        .attr("class", "node")
        .attr("data-node-id", d => d.data.id)
        .attr("transform", d => `translate(${d.y},${d.x})`)
        .style("cursor", "pointer");

      // Add circles for nodes
      node.append("circle")
        .attr("r", nodeSize)
        .style("fill", d => {
          if (!d || !d.data || !d.data.id) return nodeColors.leaf;
          if (selectedNode && selectedNode.id === d.data.id) return nodeColors.selected;
          if (searchTerm && d.data.name && (d.data.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (d.data.value && d.data.value.toLowerCase().includes(searchTerm.toLowerCase())))) {
            return nodeColors.searchMatch;
          }
          if (modifiedNodes.has(d.data.id)) return nodeColors.searchMatch;
          if (d.data.children || d.data._children) {
            return expandedNodes.has(d.data.id) ? nodeColors.expanded : nodeColors.collapsed;
          }
          return nodeColors.leaf;
        })
        .style("stroke", "#333")
        .style("stroke-width", "2px");

      // Add text labels
      node.append("text")
        .attr("dy", ".35em")
        .attr("x", d => (d && d.data && (d.data.children || d.data._children)) ? -13 : 13)
        .style("text-anchor", d => (d && d.data && (d.data.children || d.data._children)) ? "end" : "start")
        .style("fill", textColor)
        .style("font-size", "12px")
        .text(d => {
          if (!d || !d.data || !d.data.name) return 'unknown';
          const displayValue = (d.data.id && modifiedNodes.get(d.data.id)) || d.data.value;
          return `${d.data.name}${displayValue ? `: ${displayValue}` : ''}`;
        });

      // Add click handlers
      node.on("click", (event, d) => {
        event.stopPropagation();
        if (!d || !d.data || !d.data.id) return;

        if (d.data.children || d.data._children) {
          toggleNode(d.data.id);
        } else if (d.data.isEditable) {
          handleNodeEdit(d.data);
        }
        setSelectedNode(d.data);
      });

    } catch (error) {
      console.error('Error in updateVisualization:', error);
      // Zoom ref'i temizle
      zoomRef.current = null;

      const svg = d3.select(svgRef.current);
      if (svg.node()) {
        svg.selectAll("*").remove();
        svg.append("text")
          .attr("x", 50)
          .attr("y", 50)
          .text('Error rendering tree visualization')
          .style('fill', 'red');
      }
    }
  }, [treeData, nodeSize, searchTerm, selectedNode, expandedNodes, linkColor, nodeColors, textColor, backgroundColor, modifiedNodes, toggleNode]);

  // Effects - useEffect'leri de hooks kısmına dahil ediyoruz
  useEffect(() => {
    if (xmlData && typeof xmlData === 'string' && xmlData.trim().length > 0) {
      try {
        const parsed = parseXML(xmlData);
        if (parsed) {
          setTreeData(parsed);
        } else {
          console.warn('Failed to parse XML data');
          setTreeData(null);
        }
      } catch (error) {
        console.error('Error in useEffect parsing XML:', error);
        setTreeData(null);
      }
    } else {
      setTreeData(null);
    }
  }, [xmlData, parseXML]);

  useEffect(() => {
    updateVisualization();
  }, [updateVisualization]);

  // Conditional rendering'i TÜM hooks'lardan sonra yapıyoruz
  if (!xmlData || typeof xmlData !== 'string' || xmlData.trim().length === 0) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography variant="h6" color="text.secondary" gutterBottom>
          XML verisi yok
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Lütfen bir mesaj seçin
        </Typography>
      </Box>
    );
  }

  // Error handling de hooks'lardan sonra
  if (hasError) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Alert severity="error">
          {errorMessage || 'Bileşen yüklenemedi. Sayfayı yenileyin.'}
        </Alert>
      </Box>
    );
  }

  // Theme presets
  const themePresets = {
    light: {
      backgroundColor: '#ffffff',
      nodeColors: { expanded: '#4caf50', collapsed: '#ff9800', selected: '#2196f3', searchMatch: '#f44336', leaf: '#81c784' },
      linkColor: '#999999', textColor: '#333333'
    },
    dark: {
      backgroundColor: '#121212',
      nodeColors: { expanded: '#66bb6a', collapsed: '#ffb74d', selected: '#42a5f5', searchMatch: '#ef5350', leaf: '#a5d6a7' },
      linkColor: '#666666', textColor: '#ffffff'
    }
  };

  // Apply theme
  const applyTheme = (themeName) => {
    const selectedTheme = themePresets[themeName];
    if (selectedTheme) {
      setTheme(themeName);
      setBackgroundColor(selectedTheme.backgroundColor);
      setNodeColors(selectedTheme.nodeColors);
      setLinkColor(selectedTheme.linkColor);
      setTextColor(selectedTheme.textColor);
    }
  };

  // Handle node editing
  const handleNodeEdit = (node) => {
    if (!node.isEditable) return;

    setEditingNode(node);
    setEditValue(modifiedNodes.get(node.id) || node.value || '');
    setShowEditDialog(true);
  };

  // Save edit
  const handleSaveEdit = () => {
    if (editingNode && editValue !== (modifiedNodes.get(editingNode.id) || editingNode.value)) {
      setShowConfirmDialog(true);
    } else {
      setShowEditDialog(false);
    }
  };

  // Confirm edit
  const confirmEdit = () => {
    if (editingNode) {
      const newModifiedNodes = new Map(modifiedNodes);
      newModifiedNodes.set(editingNode.id, editValue);
      setModifiedNodes(newModifiedNodes);
      setHasChanges(true);

      if (onNodeEdit) {
        onNodeEdit(editingNode.id, editValue);
      }
    }

    setShowEditDialog(false);
    setShowConfirmDialog(false);
    setEditingNode(null);
  };

  // Save all changes - Show confirmation dialog first
  const handleSaveChanges = () => {
    setShowSaveConfirmDialog(true);
  };

  // Actually save changes after confirmation
  const confirmSaveChanges = () => {
    const newXML = reconstructXML();
    if (newXML && onXmlUpdate) {
      onXmlUpdate(newXML);
      setHasChanges(false);
      setModifiedNodes(new Map());
    }
    setShowSaveConfirmDialog(false);
  };

  // Zoom controls - Geliştirilmiş zoom fonksiyonları
  const handleZoomIn = () => {
    const zoom = zoomRef.current;
    if (zoom && svgRef.current && typeof zoom.scaleBy === 'function') {
      try {
        d3.select(svgRef.current).transition().duration(300).call(zoom.scaleBy, 1.5);
      } catch (error) {
        console.error('Zoom in error:', error);
      }
    }
  };

  const handleZoomOut = () => {
    const zoom = zoomRef.current;
    if (zoom && svgRef.current && typeof zoom.scaleBy === 'function') {
      try {
        d3.select(svgRef.current).transition().duration(300).call(zoom.scaleBy, 0.75);
      } catch (error) {
        console.error('Zoom out error:', error);
      }
    }
  };

  const handleResetZoom = () => {
    const zoom = zoomRef.current;
    if (zoom && svgRef.current && typeof zoom.transform === 'function') {
      try {
        d3.select(svgRef.current).transition().duration(500).call(zoom.transform, d3.zoomIdentity);
      } catch (error) {
        console.error('Reset zoom error:', error);
      }
    }
  };

  // Ağacın tamamını görmek için fit-to-view
  const handleFitToView = () => {
    const zoom = zoomRef.current;
    if (zoom && treeData && svgRef.current && typeof zoom.transform === 'function') {
      try {
        const svg = d3.select(svgRef.current);
        const gNode = svg.select('g').node();

        if (!gNode) {
          console.warn('SVG g element not found');
          return;
        }

        const bounds = gNode.getBBox();
        if (!bounds || bounds.width === 0 || bounds.height === 0) {
          console.warn('Invalid bounds for fit to view');
          return;
        }

        const width = 1200;
        const height = 800;
        const margin = { top: 20, right: 120, bottom: 20, left: 120 };

        const fullWidth = width - margin.left - margin.right;
        const fullHeight = height - margin.top - margin.bottom;

        const scale = Math.min(fullWidth / bounds.width, fullHeight / bounds.height) * 0.9;
        const translateX = (fullWidth - bounds.width * scale) / 2 - bounds.x * scale;
        const translateY = (fullHeight - bounds.height * scale) / 2 - bounds.y * scale;

        svg.transition().duration(750).call(
          zoom.transform,
          d3.zoomIdentity.translate(translateX, translateY).scale(scale)
        );
      } catch (error) {
        console.error('Fit to view error:', error);
      }
    }
  };

  // Belirli bir node'a zoom yapma
  const zoomToNode = (nodeId) => {
    const zoom = zoomRef.current;
    if (zoom && treeData && svgRef.current && nodeId && typeof zoom.transform === 'function') {
      try {
        const svg = d3.select(svgRef.current);
        const nodeElement = svg.select(`[data-node-id="${nodeId}"]`);

        if (!nodeElement.empty()) {
          const nodeElementNode = nodeElement.node();
          if (!nodeElementNode) {
            console.warn('Node element not found');
            return;
          }

          const bounds = nodeElementNode.getBBox();
          if (!bounds) {
            console.warn('Node bounds not found');
            return;
          }

          const scale = 2; // 2x zoom
          const translateX = 600 - bounds.x * scale - bounds.width * scale / 2;
          const translateY = 400 - bounds.y * scale - bounds.height * scale / 2;

          svg.transition().duration(750).call(
            zoom.transform,
            d3.zoomIdentity.translate(translateX, translateY).scale(scale)
          );
        }
      } catch (error) {
        console.error('Zoom to node error:', error);
      }
    }
  };

  return (
    <Box sx={{ p: 2 }}>
      {/* Theme Panel */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <Button
                variant="outlined"
                startIcon={<Palette />}
                onClick={() => setShowThemePanel(!showThemePanel)}
              >
                {t('d3tree.theme')}
              </Button>
            </Grid>

            {showThemePanel && (
              <>
                <Grid item xs={12} sm={6} md={3}>
                  <FormControl fullWidth size="small">
                    <InputLabel>{t('d3tree.themePreset')}</InputLabel>
                    <Select
                      value={theme}
                      label={t('d3tree.themePreset')}
                      onChange={(e) => applyTheme(e.target.value)}
                    >
                      <MenuItem value="light">
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Brightness6 />
                          {t('d3tree.lightTheme')}
                        </Box>
                      </MenuItem>
                      <MenuItem value="dark">
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Brightness4 />
                          {t('d3tree.darkTheme')}
                        </Box>
                      </MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <TextField
                    fullWidth
                    size="small"
                    label={t('d3tree.backgroundColor')}
                    type="color"
                    value={backgroundColor}
                    onChange={(e) => setBackgroundColor(e.target.value)}
                  />
                </Grid>
              </>
            )}
          </Grid>
        </CardContent>
      </Card>

      {/* Controls */}
      <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
        <Stack spacing={2}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center">
            <TextField
              size="small"
              placeholder={t('d3tree.searchPlaceholder')}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <Search sx={{ color: 'text.secondary', mr: 1 }} />
              }}
              sx={{ minWidth: 200 }}
            />

            <Stack direction="row" spacing={1}>
              <Tooltip title={t('d3tree.zoomIn')}>
                <IconButton onClick={handleZoomIn} size="small">
                  <ZoomIn />
                </IconButton>
              </Tooltip>
              <Tooltip title={t('d3tree.zoomOut')}>
                <IconButton onClick={handleZoomOut} size="small">
                  <ZoomOut />
                </IconButton>
              </Tooltip>
              <Tooltip title={t('d3tree.resetZoom')}>
                <IconButton onClick={handleResetZoom} size="small">
                  <CenterFocusStrong />
                </IconButton>
              </Tooltip>
              <Tooltip title={t('d3tree.fitToView')}>
                <IconButton onClick={handleFitToView} size="small" color="primary">
                  <SwapHoriz />
                </IconButton>
              </Tooltip>
              <Tooltip title={t('d3tree.expandAll')}>
                <IconButton onClick={() => {
                  const newExpandedNodes = new Set();
                  const collectAllNodes = (node) => {
                    if (!node || !node.id) return;
                    if (node.children && Array.isArray(node.children) && node.children.length > 0) {
                      newExpandedNodes.add(node.id);
                      node.children.forEach(child => {
                        if (child) collectAllNodes(child);
                      });
                    }
                  };
                  if (treeData) {
                    collectAllNodes(treeData);
                  }
                  setExpandedNodes(newExpandedNodes);
                }} size="small">
                  <ExpandMore />
                </IconButton>
              </Tooltip>
              <Tooltip title={t('d3tree.collapseAll')}>
                <IconButton onClick={() => setExpandedNodes(new Set())} size="small">
                  <ExpandLess />
                </IconButton>
              </Tooltip>
            </Stack>
          </Stack>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="body2">{t('d3tree.nodeSize')}:</Typography>
            <Slider
              value={nodeSize}
              onChange={(_, value) => setNodeSize(value)}
              min={5}
              max={15}
              step={1}
              sx={{ width: 100 }}
            />
            <Typography variant="body2">{nodeSize}px</Typography>
          </Box>

          {hasChanges && (
            <Alert severity="warning" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ flex: 1 }}>
                {t('d3tree.unsavedChanges')}
              </Box>
              <Button
                variant="contained"
                startIcon={<Save />}
                onClick={handleSaveChanges}
                size="small"
              >
                {t('buttons.save')}
              </Button>
            </Alert>
          )}

          {selectedNode && (
            <Box>
              <Typography variant="subtitle2">{t('d3tree.selectedNode')}:</Typography>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 1 }}>
                <Chip
                  label={`${selectedNode.name}${selectedNode.value ? `: ${selectedNode.value}` : ''}`}
                  onDelete={() => setSelectedNode(null)}
                  color={modifiedNodes.has(selectedNode.id) ? 'error' : 'default'}
                />
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => zoomToNode(selectedNode.id)}
                  startIcon={<CenterFocusStrong />}
                >
                  {t('d3tree.zoomToNode')}
                </Button>
              </Stack>
            </Box>
          )}

          {/* Değişiklik yapılan node'ları göster */}
          {modifiedNodes.size > 0 && (
            <Box>
              <Typography variant="subtitle2" sx={{ color: '#ff0000', fontWeight: 'bold' }}>
                {t('d3tree.modifiedNodes')} ({modifiedNodes.size}):
              </Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mt: 1, gap: 1 }}>
                {Array.from(modifiedNodes.entries()).map(([nodeId, value]) => {
                  const nodeName = nodeId.split('/').pop();
                  return (
                    <Chip
                      key={nodeId}
                      label={`${nodeName}: ${value.length > 15 ? value.substring(0, 15) + '...' : value}`}
                      size="small"
                      color="error"
                      variant="outlined"
                      onClick={() => zoomToNode(nodeId)}
                      sx={{
                        cursor: 'pointer',
                        '&:hover': { backgroundColor: '#ffebee' }
                      }}
                    />
                  );
                })}
              </Stack>
            </Box>
          )}
        </Stack>
      </Paper>

      {/* SVG */}
      <Paper
        elevation={3}
        sx={{
          position: 'relative',
          overflow: 'hidden',
          backgroundColor: backgroundColor,
          border: `2px solid ${linkColor}`
        }}
      >
        <svg ref={svgRef} style={{ width: '100%', height: '800px' }} />
      </Paper>

      {/* Edit Dialog */}
      <Dialog open={showEditDialog} onClose={() => setShowEditDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('d3tree.editNode')}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label={t('d3tree.enterValue')}
            fullWidth
            variant="outlined"
            value={editValue}
            onChange={(e) => setEditValue(e.target.value)}
            multiline
            rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowEditDialog(false)}>{t('buttons.cancel')}</Button>
          <Button onClick={handleSaveEdit} variant="contained">{t('buttons.save')}</Button>
        </DialogActions>
      </Dialog>

      {/* Confirm Dialog */}
      <Dialog open={showConfirmDialog} onClose={() => setShowConfirmDialog(false)}>
        <DialogTitle>{t('d3tree.confirmEdit')}</DialogTitle>
        <DialogContent>
          <Typography>{t('d3tree.confirmEditMessage')}</Typography>
          {editingNode && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2">
                <strong>{t('d3tree.oldValue')}:</strong> {editingNode.value || t('d3tree.empty')}
              </Typography>
              <Typography variant="body2">
                <strong>{t('d3tree.newValue')}:</strong> {editValue || t('d3tree.empty')}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowConfirmDialog(false)}>{t('buttons.cancel')}</Button>
          <Button onClick={confirmEdit} variant="contained">{t('buttons.confirm')}</Button>
        </DialogActions>
      </Dialog>

      {/* Save Confirm Dialog */}
      <Dialog open={showSaveConfirmDialog} onClose={() => setShowSaveConfirmDialog(false)}>
        <DialogTitle>{t('d3tree.confirmSave')}</DialogTitle>
        <DialogContent>
          <Typography>{t('d3tree.confirmSaveMessage')}</Typography>
          <Alert severity="warning" sx={{ mt: 2 }}>
            <Typography variant="body2">
              {t('d3tree.saveWarning')}
            </Typography>
          </Alert>
          {modifiedNodes.size > 0 && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                {t('d3tree.changesWillBeSaved')} ({modifiedNodes.size}):
              </Typography>
              <Box sx={{ mt: 1, maxHeight: 200, overflow: 'auto' }}>
                {Array.from(modifiedNodes.entries()).map(([nodeId, value]) => {
                  const nodeName = nodeId.split('/').pop();
                  return (
                    <Typography key={nodeId} variant="body2" sx={{ ml: 2, color: '#d32f2f' }}>
                      • <strong>{nodeName}</strong>: {value}
                    </Typography>
                  );
                })}
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowSaveConfirmDialog(false)}>{t('buttons.cancel')}</Button>
          <Button onClick={confirmSaveChanges} variant="contained" color="primary" startIcon={<Save />}>
            {t('buttons.saveChanges')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
});

export default D3Tree;
