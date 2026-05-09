# lcnpages frontend

Angular 21.x SPA for the lcnpages knowledge base.

## Local development

Prerequisites:
- Node.js 20+ LTS
- Angular CLI 21+

Install dependencies:
```bash
npm install
```

Run dev server:
```bash
ng serve
```

The app runs on `http://localhost:4200`.

## Stack notes

- Standalone components (no NgModule)
- Zoneless change detection (no zone.js)
- Vitest for unit tests (replaces Karma)
- Tailwind CSS for styling
- OIDC authentication via `angular-auth-oidc-client@21.x` (added in Step 1C)
