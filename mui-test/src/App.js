import React from "react";
import { ThemeProvider, CssBaseline, Button } from "@mui/material";
import { createTheme } from "@mui/material/styles";

const theme = createTheme();

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Button variant="contained" color="primary">
          Test Butonu
        </Button>
      </div>
    </ThemeProvider>
  );
}

export default App; 