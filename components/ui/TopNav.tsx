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

const ACCENTS = ['crimson', 'azure', 'lime', 'amber'] as const;
type Accent = (typeof ACCENTS)[number];
const ACCENT_LABEL: Record<Accent, string> = { crimson: 'Crimson', azure: 'Azure', lime: 'Lime', amber: 'Amber' };

export default function TopNav({ show }: { show: boolean }) {
  const [active, setActive] = useState(0);
  const [accent, setAccent] = useState<Accent>('crimson');
  const [menuOpen, setMenuOpen] = useState(false);
  const bar = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const saved = window.localStorage.getItem('accent') as Accent | null;
    if (saved && ACCENTS.includes(saved)) {
      setAccent(saved);
      if (saved !== 'crimson') document.documentElement.dataset.accent = saved;
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
  const cycleAccent = () => {
    const next = ACCENTS[(ACCENTS.indexOf(accent) + 1) % ACCENTS.length];
    setAccent(next);
    if (next === 'crimson') delete document.documentElement.dataset.accent;
    else document.documentElement.dataset.accent = next;
    window.localStorage.setItem('accent', next);
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
        cycleAccent();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accent]);

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
      <button className="nav-theme" onClick={cycleAccent} aria-label={`Accent theme: ${ACCENT_LABEL[accent]}. Switch theme`}>
        <i data-accent-dot={accent} />
        <span>{ACCENT_LABEL[accent]}</span>
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
