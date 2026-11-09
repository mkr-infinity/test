# Contributing

Thanks for stopping by 👋 This is a personal portfolio, but issues, ideas and
improvements are always welcome.

## Getting started

```bash
git clone https://github.com/mkr-infinity/test.git
cd test
nvm use                 # or a Node 20.x runtime
npm install
npm run dev
```

## Before you open a PR

1. **Check the typechecker**: `npm run typecheck`
2. **Build to verify**: `npm run build`
3. Keep changes focused — one PR, one idea.
4. If it changes content, it probably belongs in `lib/data.ts`, not a component.

## Code style

- The site is a client-side, scroll-driven film. UI motion belongs in
  [Framer Motion](https://www.framer.com/motion/) + the CSS in `app/globals.css`.
- New colors should derive from the accent variables (`--red / --red-d / --red-dd`)
  so theme switching keeps working.
- Accessibility: keyboard focus (`:focus-visible`) and `prefers-reduced-motion`
  are respected globally — please don't regress them.

## CI

There is no auto-triggered CI. A manual quality workflow
(`.github/workflows/quality.yml`) can be run from the Actions tab; it runs
typecheck + build.
