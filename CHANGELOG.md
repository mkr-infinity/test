# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
