# Mohammad Kaif Raja — Portfolio v2

**🎬 Live: [mkr-infinity.github.io/test](https://mkr-infinity.github.io/test/)** — deploys automatically on every push to `main`.

A hand-built, framework-free portfolio: plain HTML, one CSS file, ~40 lines of JavaScript. No build step, no dependencies, nothing to install. The whole site is a few tens of kilobytes and loads instantly on any device.

![Portfolio preview](assets/og.jpg)

## Design

- **Dark & light themes** — follows your system preference out of the box, one-click toggle in the header, remembered per visitor. Set *before* first paint, so there's never a flash of the wrong theme.
- **Lightweight by design** — system font stacks (zero font downloads), CSS-only animations with `IntersectionObserver` reveals, `prefers-reduced-motion` respected everywhere.
- **Every device** — fluid from 320px phones to ultra-wide desktops, landscape tuning, `≥44px` touch targets, skip link, focus-visible styles, semantic HTML throughout.

## Content

| Section | What's there |
|---|---|
| Missions | 6 real open-source builds (GSI Flashing Guide ⭐68, Revision Master ⭐33, ReadmeHub ⭐25, MKR Music ⭐16, CalcVerse, Solo-Ledger ⭐13) |
| Field Guides | 5 real guides with star counts and live links |
| Journey | The distro-hopping → Android modding → open-source builder arcs |
| Contact | Email, GitHub, Telegram, Instagram |

## Editing

Everything is plain files — open and edit:

- `index.html` — all content and structure
- `styles.css` — design tokens at the top (`:root` dark, `html[data-theme='light']` light)
- `script.js` — theme toggle + scroll reveal
- `assets/` — favicon, OG image, PWA icons
- `manifest.webmanifest`, `robots.txt`, `sitemap.xml` — metadata

## Deploying

GitHub Pages does it: `.github/workflows/deploy-site.yml` copies the site files and publishes on every push to `main` (or manually from the Actions tab). No build, no npm — the workflow just collects static files.

---

<sub>MIT licensed. Hand-built with HTML, CSS and a few lines of JS.</sub>
