import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    outDir: '../../rpa-system/rpa-application/src/main/resources/static',
    emptyOutDir: true
  },
  server: {
    port: 3000,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/task': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/process': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/robot': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/execute': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
