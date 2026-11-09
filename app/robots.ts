import type { MetadataRoute } from 'next';
import { BASE, SITE } from '@/lib/paths';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: { userAgent: '*', allow: '/' },
    sitemap: `${SITE}${BASE}/sitemap.xml`,
  };
}
