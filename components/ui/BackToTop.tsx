'use client';

import { useEffect, useState } from 'react';
import { scrollToId } from './SmoothScroll';

export default function BackToTop() {
  const [show, setShow] = useState(false);
  useEffect(() => {
    const onScroll = () => setShow(window.scrollY > window.innerHeight * 1.5);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);
  return (
    <button
      className={`btt${show ? ' on' : ''}`}
      onClick={() => scrollToId('billboard')}
      aria-label="Back to top"
      tabIndex={show ? 0 : -1}
    >
      ↑
    </button>
  );
}
