import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const bypassSpaPageRequest = (req) => {
  if (req.method === 'GET' && req.headers.accept?.includes('text/html')) {
    return '/index.html'
  }
}

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
    headers: {
      'Cache-Control': 'no-cache, no-store, must-revalidate'
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
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
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/process': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/robot': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/execute': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/execution': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/data': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/ws-task': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      },
      '/batch': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/schedule': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: bypassSpaPageRequest
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
