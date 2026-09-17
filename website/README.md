# Cloudflare Pages

Connect this repo in Cloudflare Pages with:

| Setting | Value |
|---------|--------|
| Root directory | `website` |
| Build command | `npm run build` |
| Output directory | `dist` |
| Node | 18+ |

Custom domain: `fuellog.chandanshakya.com.np`

Local build:

```bash
cd website
npm install
npm run build
```

Upload `website/dist` if you publish manually.
