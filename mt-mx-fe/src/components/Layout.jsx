import React from 'react';
import { Box, AppBar, Toolbar, Typography, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, CssBaseline, Button, ButtonGroup } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import InboxIcon from '@mui/icons-material/MoveToInbox';
import MailIcon from '@mui/icons-material/Mail';
import Notification from './Notification';
import { useMessages } from '../context/MessageContext';

const drawerWidth = 240;

const Layout = ({ children }) => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { notification, closeNotification } = useMessages();

  const menuItems = [
    { text: 'menu_mt102', path: '/mt102' },
    { text: 'menu_mt103', path: '/mt103' },
    { text: 'menu_mt202', path: '/mt202' },
    { text: 'menu_mt203', path: '/mt203' }
  ];

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <Notification
        open={notification.open}
        handleClose={closeNotification}
        message={notification.message}
        severity={notification.severity}
      />
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {t('app_title')}
          </Typography>
          
          <ButtonGroup variant="outlined" aria-label="language selection">
            <Button onClick={() => changeLanguage('en')}>EN</Button>
            <Button onClick={() => changeLanguage('tr')}>TR</Button>
          </ButtonGroup>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {menuItems.map((item, index) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  selected={location.pathname === item.path}
                  onClick={() => navigate(item.path)}
                  aria-label={item.text}
                >
                  <ListItemIcon>
                    {index % 2 === 0 ? <InboxIcon /> : <MailIcon />}
                  </ListItemIcon>
                  <ListItemText primary={t(item.text)} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default Layout;
