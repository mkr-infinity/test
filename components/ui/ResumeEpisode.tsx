'use client';

import { useEffect, useState } from 'react';
import { playTo } from './SmoothScroll';

const KEY = 'unmasked-resume';

export default function ResumeEpisode() {
  const [saved, setSaved] = useState<{ y: number; pct: number } | null>(null);

  useEffect(() => {
    const y = Number(window.localStorage.getItem(KEY) || 0);
    if (y > window.innerHeight * 2) {
      const max = document.documentElement.scrollHeight - window.innerHeight;
      setSaved({ y, pct: Math.min(99, Math.max(1, Math.round((y / max) * 100))) });
    }
    let t = 0;
    const onScroll = () => {
      window.clearTimeout(t);
      t = window.setTimeout(() => window.localStorage.setItem(KEY, String(window.scrollY)), 400);
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', onScroll);
      window.clearTimeout(t);
    };
  }, []);

  if (!saved) return null;

  const dismiss = () => {
    window.localStorage.removeItem(KEY);
    setSaved(null);
  };

  return (
    <div className="resume-toast" role="status">
      <p className="rt-label">
        <i />
        EPISODE IN PROGRESS · {saved.pct}%
      </p>
      <div className="rt-actions">
        <button
          className="rt-resume"
          onClick={() => {
            playTo(saved.y, 2.2);
            dismiss();
          }}
        >
          ▶ Resume
        </button>
        <button className="rt-restart" onClick={dismiss}>
          Start over
        </button>
      </div>
    </div>
  );
}
