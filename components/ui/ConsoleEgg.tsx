'use client';

import { useEffect } from 'react';

export default function ConsoleEgg() {
  useEffect(() => {
    console.log(
      '%cKAKASHI: UNMASKED',
      'color:#e50914;font-size:24px;font-weight:bold;letter-spacing:4px;text-shadow:0 0 12px rgba(229,9,20,.6)',
    );
    console.log(
      '%cYou opened the hidden scroll. Curiosity suits you.',
      'color:#b3b3b3;font-size:12px;line-height:1.6',
    );
    console.log(
      '%cThe source of every mission → https://github.com/mkr-infinity',
      'color:#f5b301;font-size:12px',
    );
  }, []);
  return null;
}
