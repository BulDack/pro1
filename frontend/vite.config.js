import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';



// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      // '@'를 'src' 디렉터리의 절대 경로로 매핑합니다.
      '@': path.resolve(__dirname, './src'),
    },
  },
});