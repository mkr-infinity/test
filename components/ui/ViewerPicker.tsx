'use client';

import { useEffect, useState, type CSSProperties } from 'react';
import { VIEWERS } from '@/lib/data';

const KEY = 'unmasked-viewer';

const ICON: Record<string, string> = { eye: '👁️', kunai: '🌀', leaf: '🍃', shuriken: '✴️' };

/** Netflix-style "Who's watching?" gate — remembers the returning visitor, never blocks again. */
export default function ViewerPicker() {
  const [show, setShow] = useState(false);
  useEffect(() => {
    if (!window.localStorage.getItem(KEY)) setShow(true);
  }, []);
  if (!show) return null;
  const pick = (id: string) => {
    window.localStorage.setItem(KEY, id);
    setShow(false);
  };
  return (
    <div className="vp" role="dialog" aria-modal="true" aria-label="Who’s watching?">
      <p className="vp-pre">UNMASKED</p>
      <h2 className="vp-title">
        Who’s <span>watching?</span>
      </h2>
      <div className="vp-row">
        {VIEWERS.map((v) => (
          <button key={v.id} className="vp-card" style={{ '--vc': v.color } as CSSProperties} onClick={() => pick(v.id)}>
            <i aria-hidden>{ICON[v.icon]}</i>
            <b>{v.name}</b>
            <span>{v.line}</span>
          </button>
        ))}
      </div>
      <button className="vp-skip" onClick={() => setShow(false)}>
        Skip — just play the movie
      </button>
    </div>
  );
}
