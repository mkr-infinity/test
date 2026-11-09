import Link from 'next/link';
import { BASE } from '@/lib/paths';

export default function NotFound() {
  return (
    <main
      style={{
        minHeight: '100svh',
        display: 'grid',
        placeItems: 'center',
        background: '#000',
        color: '#fff',
        textAlign: 'center',
        padding: '0 24px',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <img
        src={`${BASE}/stills/masked.jpg`}
        alt=""
        aria-hidden
        style={{
          position: 'absolute',
          right: 0,
          top: 0,
          height: '100%',
          width: '52%',
          objectFit: 'cover',
          opacity: 0.5,
          maskImage: 'linear-gradient(to right, transparent, black 55%)',
          WebkitMaskImage: 'linear-gradient(to right, transparent, black 55%)',
        }}
      />
      <div
        aria-hidden
        style={{
          position: 'absolute',
          inset: 0,
          background: 'radial-gradient(50% 45% at 50% 55%, rgba(229, 9, 20, 0.18), transparent 70%)',
          pointerEvents: 'none',
        }}
      />
      <div style={{ position: 'relative' }}>
        <p style={{ fontFamily: 'var(--mono)', fontSize: 12, letterSpacing: '0.4em', color: 'var(--red)', marginBottom: 18 }}>
          SCENE MISSING
        </p>
        <h1 style={{ fontFamily: 'var(--display)', fontWeight: 400, fontSize: 'clamp(72px, 14vw, 220px)', lineHeight: 0.85 }}>
          40<span style={{ color: 'var(--red)' }}>4</span>
        </h1>
        <p style={{ color: 'var(--muted)', maxWidth: 420, margin: '22px auto 34px', lineHeight: 1.6 }}>
          This episode was never animated. The scroll you followed leads nowhere — rewind back to the prologue.
        </p>
        <Link href="/" className="btn-red">
          ← Back to the movie
        </Link>
      </div>
    </main>
  );
}
