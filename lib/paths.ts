/** Site origin — where this portfolio is published. */
export const SITE = 'https://mkr-infinity.github.io';

/**
 * Base path for static hosting under a sub-path (e.g. GitHub Pages project sites
 * serve at /test/). Set NEXT_PUBLIC_BASE_PATH=/test at build time; empty for
 * root deployments (Vercel, localhost).
 */
export const BASE = process.env.NEXT_PUBLIC_BASE_PATH || '';
