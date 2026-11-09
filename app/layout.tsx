import type { Metadata, Viewport } from 'next';
import '@fontsource/bebas-neue/400.css';
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/inter/600.css';
import '@fontsource/inter/700.css';
import '@fontsource/inter/800.css';
import '@fontsource/jetbrains-mono/400.css';
import './globals.css';

export const metadata: Metadata = {
  metadataBase: new URL('https://mkr-infinity.github.io'),
  title: 'Mohammad Kaif Raja · Unmasked · System Optimizer & Open-Source Builder',
  description:
    'Everyone wanted to know what’s under the mask. Scroll to play the episode — Android flashing, Linux tweaking and open-source building, directed by curiosity.',
  keywords: ['Mohammad Kaif Raja', 'mkr-infinity', 'portfolio', 'open source', 'Linux', 'Android modding', 'system optimization'],
  authors: [{ name: 'Mohammad Kaif Raja', url: 'https://github.com/mkr-infinity' }],
  openGraph: {
    title: 'Kakashi: Unmasked — the portfolio of Kaif Raja',
    description: 'What’s under the mask? Scroll to play. Systems, Android and open source, anime-style.',
    url: 'https://mkr-infinity.github.io',
    siteName: 'Kakashi: Unmasked',
    type: 'website',
    images: [{ url: '/stills/face.jpg', width: 1200, height: 630, alt: 'Kakashi: Unmasked — anime portfolio' }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Kakashi: Unmasked — the portfolio of Kaif Raja',
    description: 'What’s under the mask? Scroll to play.',
    images: ['/stills/face.jpg'],
  },
  icons: { icon: '/stills/masked.jpg' },
};

export const viewport: Viewport = { themeColor: '#000000', width: 'device-width', initialScale: 1 };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
