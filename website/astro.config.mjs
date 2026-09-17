import { defineConfig } from 'astro/config';

export default defineConfig({
  site: 'https://fuellog.chandanshakya.com.np',
  compressHTML: true,
  build: {
    inlineStylesheets: 'always',
  },
});
