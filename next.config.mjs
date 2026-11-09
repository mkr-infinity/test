/** @type {import('next').NextConfig} */
const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';
const nextConfig = {
  reactStrictMode: true,
  poweredByHeader: false,
  transpilePackages: ['three'],
  images: { unoptimized: true },
  // GitHub Pages (project site) needs a static export under /test/;
  // root deployments (Vercel, localhost) build as normal.
  ...(basePath ? { basePath, trailingSlash: true, output: 'export' } : {}),
};
export default nextConfig;
