# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Lightweight for low-end devices** — unused Inter 700/800 font files removed, Bangers display font preloaded, static fog frame under `prefers-reduced-motion`, no WebGL fog on `Save-Data`/2G, `≥44px` tap targets on touch devices, landscape-phone layout tuning ([#56](https://github.com/mkr-infinity/test/pull/56)–[#58](https://github.com/mkr-infinity/test/pull/58))
- **5 full website skins** — the theme switcher now cycles whole-website styles, each replacing palette, font system, background world and structural treatment ([#51](https://github.com/mkr-infinity/test/pull/51)–[#54](https://github.com/mkr-infinity/test/pull/54)):
  - **Cinema** (default) — the original crimson film look
  - **Arcade** — neon cyan on synth black, mono type everywhere, glow shadows
  - **Manga Ink** — white paper, ink-black comic panels, halftone dots, hard offset shadows (full light mode)
  - **Editorial Noir** — cream paper, Georgia serif throughout, muted burgundy, magazine typography (full light mode)
  - **Terminal** — phosphor green CRT with scanlines and outline buttons
- **"Who's watching?" viewer gate** — the previously unused `VIEWERS` personas now render as a Netflix-style profile picker, remembered per visitor ([#47](https://github.com/mkr-infinity/test/pull/47))
- Missions expanded to **6 real episodes** — ReadmeHub, CalcVerse and Solo-Ledger join the board ([#41](https://github.com/mkr-infinity/test/pull/41))
- "Try live ↗" links on guides with live deployments ([#48](https://github.com/mkr-infinity/test/pull/48))
- Console easter egg and ⏸ tab-pause title ([#40](https://github.com/mkr-infinity/test/pull/40), [#44](https://github.com/mkr-infinity/test/pull/44))
- Data-saver mode: `Save-Data`/2G clients load fewer frames at low concurrency ([#42](https://github.com/mkr-infinity/test/pull/42))
- Focus trap + `inert` background for the mobile menu ([#43](https://github.com/mkr-infinity/test/pull/43))
- Instagram contact link ([#45](https://github.com/mkr-infinity/test/pull/45))
- `.gitattributes` hygiene and apple-web-app metadata ([#50](https://github.com/mkr-infinity/test/pull/50), [#49](https://github.com/mkr-infinity/test/pull/49))

### Changed
- Accent color cycle replaced by the skin system; the `T` shortcut and nav button now cycle skins ([#54](https://github.com/mkr-infinity/test/pull/54))
- README features/structure docs refreshed ([#46](https://github.com/mkr-infinity/test/pull/46), [#55](https://github.com/mkr-infinity/test/pull/55))

## [2.0.0] — 2026-11-09

### The rewrite

- **Portfolio rebuilt from scratch as a framework-free static site** — plain HTML + one CSS file + ~40 lines of JS. No Next.js, no React, no three.js, no framer-motion, no build step, no dependencies. Total payload is a few tens of KB and loads instantly on any device ([#62](https://github.com/mkr-infinity/test/pull/62))
- **Dark & light themes** — system-preference aware, header toggle, persisted, set before first paint (no theme flash)
- All real content retained: 6 missions, 5 field guides with live links, the 3-arc journey, stats (30+ repos / 428+ stars / 68⭐ top repo) and every contact channel
- Deploy workflow rewritten for the no-build flow; GitHub Pages publishing unchanged
- The v1 anime-movie experience remains available in git history and the `v1.1.0` tag

## [1.1.0] — 2026-11-09

### Added
- **Real profile & content** — the portfolio now presents Mohammad Kaif Raja (system optimizer & open-source builder) with real repos, stats and links; the placeholder demo profile is gone ([#4](https://github.com/mkr-infinity/test/pull/4))
- **Field Guides chapter** — a new section showcasing the guide repos (GSI flashing 68⭐, bootloader unlock 27⭐, drivers, cheatsheet, rooted-device methods) ([#32](https://github.com/mkr-infinity/test/pull/32)–[#34](https://github.com/mkr-infinity/test/pull/34))
- **4 accent themes** (Crimson / Azure / Lime / Amber) with a persisted nav switcher ([#15](https://github.com/mkr-infinity/test/pull/15), [#16](https://github.com/mkr-infinity/test/pull/16))
- **"Continue watching"** — scroll position persists per visitor; returning visitors can resume like a streaming app ([#38](https://github.com/mkr-infinity/test/pull/38))
- **Keyboard shortcuts** — `←`/`→` between chapters, `T` cycles theme ([#36](https://github.com/mkr-infinity/test/pull/36))
- **Mobile chapters menu** — hamburger overlay for small screens, where the pill nav was previously hidden entirely ([#20](https://github.com/mkr-infinity/test/pull/20))
- Floating back-to-top button ([#35](https://github.com/mkr-infinity/test/pull/35))
- Themed 404 with the masked still, branded error boundary and route loading screen ([#11](https://github.com/mkr-infinity/test/pull/11), [#26](https://github.com/mkr-infinity/test/pull/26), [#28](https://github.com/mkr-infinity/test/pull/28), [#24](https://github.com/mkr-infinity/test/pull/24))
- SEO: real metadata, OpenGraph/Twitter cards with a dedicated 1200×630 banner, robots.txt, sitemap, JSON-LD Person schema ([#7](https://github.com/mkr-infinity/test/pull/7), [#22](https://github.com/mkr-infinity/test/pull/22), [#11](https://github.com/mkr-infinity/test/pull/11), [#30](https://github.com/mkr-infinity/test/pull/30))
- PWA manifest with installable 192/512px icons ([#29](https://github.com/mkr-infinity/test/pull/29))
- Proper favicon set — flat `KR` monogram mirroring the nav mark ([#21](https://github.com/mkr-infinity/test/pull/21))
- Manual-only CI workflow (`npm ci` → typecheck → build), `npm run typecheck` script, Node version pinning ([#10](https://github.com/mkr-infinity/test/pull/10), [#12](https://github.com/mkr-infinity/test/pull/12), [#18](https://github.com/mkr-infinity/test/pull/18))
- Community files: MIT LICENSE, CONTRIBUTING.md, FUNDING.yml ([#19](https://github.com/mkr-infinity/test/pull/19), [#37](https://github.com/mkr-infinity/test/pull/37))

### Changed
- README rewritten for this repo — tech stack, structure, scripts, badges, live CI status ([#6](https://github.com/mkr-infinity/test/pull/6), [#17](https://github.com/mkr-infinity/test/pull/17), [#23](https://github.com/mkr-infinity/test/pull/23), [#31](https://github.com/mkr-infinity/test/pull/31))
- Contact links now real: Telegram instead of a placeholder LinkedIn; the dead `resume.pdf` link points at the GitHub repos ([#4](https://github.com/mkr-infinity/test/pull/4))

### Fixed
- Nav monogram still showed the original demo author's initials (LD → KR) ([#8](https://github.com/mkr-infinity/test/pull/8))
- Honors marquee labeled "Certifications" while listing projects/tools ([#12](https://github.com/mkr-infinity/test/pull/12))

### Accessibility
- `:focus-visible` styles, `aria-current` on chapters, skip-to-content link, reduced-motion guard, touch-safe cursor fallback ([#8](https://github.com/mkr-infinity/test/pull/8), [#27](https://github.com/mkr-infinity/test/pull/27))
- Small-phone (≤480px) refinements: 2-column stat dials, full-width tap targets ([#9](https://github.com/mkr-infinity/test/pull/9))

### Performance
- Async image decoding, dark color-scheme hint, branded scrollbar, balanced headings ([#14](https://github.com/mkr-infinity/test/pull/14), [#13](https://github.com/mkr-infinity/test/pull/13))

## [1.0.0] — initial import of the anime-portfolio template
