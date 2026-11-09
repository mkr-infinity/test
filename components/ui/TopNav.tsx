'use client';

import { motion } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';
import { PROFILE } from '@/lib/data';
import { scrollToId } from './SmoothScroll';

export const CHAPTERS = [
  { id: 'billboard', label: 'Prologue' },
  { id: 'about', label: 'Origin' },
  { id: 'skills', label: 'Arsenal' },
  { id: 'projects', label: 'Missions' },
  { id: 'honors', label: 'Honors' },
  { id: 'guides', label: 'Guides' },
  { id: 'journey', label: 'Journey' },
  { id: 'contact', label: 'Finale' },
];

const THEMES = [
  { id: 'cinema', label: 'Cinema', dot: '#e50914' },
  { id: 'arcade', label: 'Arcade', dot: '#00f0ff' },
  { id: 'manga', label: 'Manga', dot: '#101010' },
  { id: 'noir', label: 'Noir', dot: '#9a2b2b' },
  { id: 'terminal', label: 'Terminal', dot: '#3dff6e' },
] as const;
type Theme = (typeof THEMES)[number]['id'];
const THEME_LABEL: Record<Theme, string> = { cinema: 'Cinema', arcade: 'Arcade', manga: 'Manga', noir: 'Noir', terminal: 'Terminal' };
const THEME_DOT: Record<Theme, string> = { cinema: '#e50914', arcade: '#00f0ff', manga: '#101010', noir: '#9a2b2b', terminal: '#3dff6e' };
const THEME_KEY = 'unmasked-theme';

export default function TopNav({ show }: { show: boolean }) {
  const [active, setActive] = useState(0);
  const [theme, setTheme] = useState<Theme>('cinema');
  const [menuOpen, setMenuOpen] = useState(false);
  const bar = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const saved = window.localStorage.getItem(THEME_KEY) as Theme | null;
    if (saved && THEMES.some((t) => t.id === saved)) {
      setTheme(saved);
      if (saved !== 'cinema') document.documentElement.dataset.theme = saved;
    }
  }, []);
  const burgerRef = useRef<HTMLButtonElement>(null);
  const overlayRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const main = document.getElementById('top');
    if (menuOpen) {
      main?.setAttribute('inert', '');
      overlayRef.current?.querySelectorAll<HTMLElement>('button')[0]?.focus();
    } else {
      main?.removeAttribute('inert');
    }
    if (!menuOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setMenuOpen(false);
        burgerRef.current?.focus();
        return;
      }
      if (e.key !== 'Tab') return;
      const items = Array.from(overlayRef.current?.querySelectorAll<HTMLElement>('button') ?? []);
      if (!items.length) return;
      const first = items[0];
      const last = items[items.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [menuOpen]);
  const goChapter = (id: string) => {
    setMenuOpen(false);
    scrollToId(id);
  };
  const cycleTheme = () => {
    const next = THEMES[(THEMES.findIndex((t) => t.id === theme) + 1) % THEMES.length].id;
    setTheme(next);
    if (next === 'cinema') delete document.documentElement.dataset.theme;
    else document.documentElement.dataset.theme = next;
    window.localStorage.setItem(THEME_KEY, next);
  };
  const activeRef = useRef(0);
  useEffect(() => {
    let raf = 0;
    const t = () => {
      const max = document.documentElement.scrollHeight - window.innerHeight;
      if (bar.current) bar.current.style.transform = `scaleX(${max > 0 ? window.scrollY / max : 0})`;
      const mid = window.innerHeight * 0.45;
      let a = 0;
      CHAPTERS.forEach((c, i) => {
        const el = document.getElementById(c.id);
        if (el && el.getBoundingClientRect().top <= mid) a = i;
      });
      activeRef.current = a;
      setActive((x) => (x === a ? x : a));
      raf = requestAnimationFrame(t);
    };
    raf = requestAnimationFrame(t);
    return () => cancelAnimationFrame(raf);
  }, []);
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      const t = e.target as HTMLElement | null;
      if (t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable)) return;
      if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
        e.preventDefault();
        const dir = e.key === 'ArrowRight' ? 1 : -1;
        const next = Math.min(CHAPTERS.length - 1, Math.max(0, activeRef.current + dir));
        scrollToId(CHAPTERS[next].id);
      } else if (e.key.toLowerCase() === 't') {
        cycleTheme();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [theme]);

  return (
    <motion.header className="nav" initial={{ y: -40, opacity: 0 }} animate={{ y: show ? 0 : -40, opacity: show ? 1 : 0 }} transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}>
      <div className="nav-progress" ref={bar} />
      <button className="nav-mark" onClick={() => scrollToId('billboard')} aria-label="Back to top">
        <span className="nm-sq">KR</span>
        <span className="nm-txt">
          {PROFILE.realName}
          <small>{PROFILE.role}</small>
        </span>
      </button>
      <button
        ref={burgerRef}
        className="nav-burger"
        aria-expanded={menuOpen}
        aria-label={menuOpen ? 'Close chapters menu' : 'Open chapters menu'}
        onClick={() => setMenuOpen((o) => !o)}
      >
        <i />
        <i />
        <i />
      </button>
      <nav className="nav-chapters" aria-label="Chapters">
        {CHAPTERS.map((c, i) => (
          <button key={c.id} className={i === active ? 'on' : ''} aria-current={i === active ? 'true' : undefined} onClick={() => scrollToId(c.id)}>
            <i>{String(i).padStart(2, '0')}</i>
            <span>{c.label}</span>
          </button>
        ))}
      </nav>
      <button className="nav-theme" onClick={cycleTheme} aria-label={`Theme: ${THEME_LABEL[theme]}. Switch skin`}>
        <i style={{ background: THEME_DOT[theme] }} />
        <span>{THEME_LABEL[theme]}</span>
      </button>
      <a className="nav-cta" href={`mailto:${PROFILE.email}`}>
        <span>Hire me</span>
      </a>
      {menuOpen && (
        <div ref={overlayRef} className="nav-overlay" role="dialog" aria-modal="true" aria-label="Chapters menu">
          {CHAPTERS.map((c, i) => (
            <button key={c.id} className={i === active ? 'on' : ''} aria-current={i === active ? 'true' : undefined} onClick={() => goChapter(c.id)}>
              <i>{String(i).padStart(2, '0')}</i>
              <span>{c.label}</span>
            </button>
          ))}
        </div>
      )}
    </motion.header>
  );
}
