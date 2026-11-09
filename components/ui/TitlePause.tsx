'use client';

import { useEffect } from 'react';

export default function TitlePause() {
  useEffect(() => {
    const base = document.title;
    const onVis = () => {
      document.title = document.hidden ? '⏸ Paused — the movie misses you' : base;
    };
    document.addEventListener('visibilitychange', onVis);
    return () => {
      document.removeEventListener('visibilitychange', onVis);
      document.title = base;
    };
  }, []);
  return null;
}
