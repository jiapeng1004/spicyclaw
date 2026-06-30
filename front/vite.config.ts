import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const isElectron = env.VITE_ELECTRON === '1'

  return {
    base: isElectron ? './' : '/',
    plugins: [vue()],
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        [env.VITE_API_BASE_URL || '/api']: {
          target: env.VITE_DEV_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})
