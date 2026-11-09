# Kakashi: Unmasked 🍥 — an anime-movie portfolio

A cinematic, scroll-driven portfolio that plays like an anime film. Scroll to play the scene, stop and time stops with you, scroll up to rewind. Red smoke, a 3D skill drum, stacking mission cards, spinning seals and a credits roll at the end.

**More projects & guides:** [github.com/mkr-infinity](https://github.com/mkr-infinity) · [mkr-infinity.github.io](https://mkr-infinity.github.io/)

## Tech Stack

- [Next.js 14](https://nextjs.org) (App Router) + TypeScript
- [React Three Fiber](https://docs.pmnd.rs/react-three-fiber) + drei + postprocessing (WebGL stage)
- [Framer Motion](https://www.framer.com/motion/) (scroll-linked animation)
- [Lenis](https://lenis.darkroom.engineering) (smooth scrolling)

## Make it yours (in 5 steps)

1. **Clone it**
   ```bash
   git clone https://github.com/mkr-infinity/test.git
   cd test
   ```
2. **Install & run**
   ```bash
   npm install
   npm run dev
   ```
   Open http://localhost:3000 🎬
3. **Put your story in** — change your name, links, projects, skills and story lines in `lib/data.ts`. Every section reads from there.
4. **Swap the photos** — replace the images in `public/stills/`, and put your character frames in `public/frames/hero-hd` and `public/frames/about-hd` (`001.webp`, `002.webp`, …).
5. **Put it online** — push to your GitHub and import it on [Vercel](https://vercel.com) (free, one click). Netlify or any cloud that runs Next.js works too.

## Project structure

```
app/                  # Next.js App Router pages, layout, global styles
components/
  sections/           # The movie: Billboard, Missions, Arsenal, Journey, Honors, Finale…
  stage/              # WebGL character stage and backdrop
  ui/                 # Nav, intro, custom cursor, smooth scroll
lib/
  data.ts             # ✏️ ALL your content lives here
  frames.ts / store.ts / useScroll.ts   # Frame sequencing & scroll plumbing
public/
  frames/             # Image sequences for the scroll-driven scenes
  stills/             # Key art for the mission cards
  fonts/              # Display fonts
```

## Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Start the dev server at `localhost:3000` |
| `npm run build` | Production build |
| `npm start` | Serve the production build |

---

<sub>Built with Next.js, Framer Motion, Lenis and a little WebGL. The anime character is fan art for a personal portfolio — use your own artwork if you publish commercially.</sub>
