# Allocentra Frontend

Modern React + TypeScript frontend with **Ops Command Center** dark theme.

## Tech Stack

- **React 18** + **TypeScript**
- **Vite** - Lightning fast build tool
- **TanStack Query** - Data fetching and caching
- **React Router** - Navigation
- **Tailwind CSS** - Utility-first styling
- **Zod** - TypeScript-first validation
- **Lucide React** - Beautiful icons

## Quick Start

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

App available at `http://localhost:5173`

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
src/
├── main.tsx              # Entry point
├── App.tsx               # App router
├── index.css             # Global styles + Tailwind
├── components/           # Reusable components
│   └── Layout.tsx        # Main app layout with sidebar
├── pages/                # Page components
│   ├── Dashboard.tsx     # Command center overview
│   ├── Cycles.tsx        # Manage allocation cycles
│   ├── Requests.tsx      # Submit requests
│   ├── RunAllocation.tsx # Execute allocation
│   ├── Results.tsx       # View results
│   ├── ScenarioLab.tsx   # Scenario simulation
│   └── AuditLog.tsx      # Audit trail
└── lib/                  # Utilities
    ├── api.ts            # API client
    └── utils.ts          # Helper functions
```

## Features

### Ops Command Center Theme

- **Dark Mode First**: Professional operations dashboard aesthetic
- **Status Cards**: Big, clear indicators for key metrics
- **Readiness Indicators**: Real-time system status
- **Alert Panels**: Clear constraint violations
- **Sidebar Navigation**: Quick access to all features

### Pages

1. **Dashboard** - Operations overview with active cycles and system status
2. **Cycles** - Create and manage allocation cycles
3. **Requests** - Submit and track resource requests
4. **Run Allocation** - Execute allocation engine
5. **Results** - View results with explanations
6. **Scenario Lab** - What-if analysis
7. **Audit Log** - Complete run history

## API Integration

Frontend connects to backend at `http://localhost:8080/api`

Proxy configured in `vite.config.ts`:

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

## Development

### Add New Component

```bash
# Create component
touch src/components/MyComponent.tsx
```

### Add New Page

```bash
# Create page
touch src/pages/MyPage.tsx

# Add route in App.tsx
<Route path="/mypage" element={<MyPage />} />
```

### API Calls

Use TanStack Query for data fetching:

```typescript
const { data, isLoading } = useQuery({
  queryKey: ['myData'],
  queryFn: api.getMyData,
})
```

## Styling

### Tailwind Classes

Custom ops theme colors:

- `bg-ops-bg` - Main background (#0a0e1a)
- `bg-ops-surface` - Card/panel background (#111827)
- `border-ops-border` - Borders (#1f2937)
- `text-ops-text` - Primary text (#f3f4f6)
- `text-ops-muted` - Secondary text (#9ca3af)
- `text-ops-accent` - Accent color (#3b82f6)
- `text-ops-success` - Success green (#10b981)
- `text-ops-warning` - Warning orange (#f59e0b)
- `text-ops-error` - Error red (#ef4444)

### Example Component

```tsx
<div className="bg-ops-surface rounded-lg border border-ops-border p-6">
  <h2 className="text-xl font-bold text-ops-text mb-4">Title</h2>
  <p className="text-sm text-ops-muted">Description</p>
</div>
```

## Environment Variables

Create `.env` file:

```
VITE_API_URL=http://localhost:8080/api
```

Access in code:

```typescript
const apiUrl = import.meta.env.VITE_API_URL
```

## Deployment

### Vercel

```bash
npm install -g vercel
vercel
```

### Netlify

```bash
npm run build
# Upload dist/ folder to Netlify
```

### Docker

```dockerfile
FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

## License

MIT
