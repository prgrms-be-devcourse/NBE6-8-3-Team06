import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  // Docker용 standalone 모드 활성화
  output: 'standalone',

  transpilePackages: ['@mui/material', '@mui/icons-material', '@mui/x-data-grid'],
  typescript: {
    ignoreBuildErrors: true,  // TypeScript 에러 무시
  },
  eslint: {
    ignoreDuringBuilds: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
        port: '',
        pathname: '/**',
      }
    ]
  }
};

export default nextConfig;
