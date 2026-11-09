# Contributing

Thanks for stopping by 👋 This portfolio is **framework-free static files** — no build step, no dependencies, nothing to install.

## Editing

- `index.html` — all content and structure
- `styles.css` — design tokens live at the top (`:root` for dark, `html[data-theme='light']` for light)
- `script.js` — theme toggle + scroll reveal only; keep it tiny

## Before you open a PR

1. Open `index.html` in a browser and check **both themes** (header toggle).
2. Check it on a phone-width viewport.
3. Keep payloads tiny: no font files, no icon libraries, no frameworks. Use system fonts and inline SVG.

## Conventions

- Colors flow through the CSS custom properties — never hard-code hex values in component rules.
- `prefers-reduced-motion` is respected globally; new animations must be too.
- Accessibility isn't optional: semantic HTML, visible focus, labelled controls.

## CI

There is no build. `.github/workflows/deploy-pages.yml` publishes to GitHub Pages on every push to `main` — your PR goes live the moment it merges, so make it count.
