import type { MetadataRoute } from 'next';
import { BASE, SITE } from '@/lib/paths';

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: `${SITE}${BASE}/`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 1,
    },
  ];
}
