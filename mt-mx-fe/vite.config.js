import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 3000,
    strictPort: true,
    watch: {
      usePolling: true,
    },
  },
  define: {
    global: 'globalThis'
  },
  build: {
    rollupOptions: {
      output: {
        globals: {
          global: 'globalThis'
        }
      }
    }
  },
  optimizeDeps: {
    include: [
      '@mui/material',
      '@mui/icons-material',
      '@emotion/react',
      '@emotion/styled'
    ]
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    teardownTimeout: 10000,
    hookTimeout: 10000,
    testTimeout: 10000,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.config.js',
        '**/*.config.ts',
      ],
    },
  },
}) 