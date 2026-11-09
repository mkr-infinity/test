'use client';

export default function Error({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
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
      }}
    >
      <div>
        <p style={{ fontFamily: 'var(--mono)', fontSize: 12, letterSpacing: '0.4em', color: 'var(--red)', marginBottom: 18 }}>
          PROJECTOR MALFUNCTION
        </p>
        <h1 style={{ fontFamily: 'var(--display)', fontWeight: 400, fontSize: 'clamp(56px, 10vw, 140px)', lineHeight: 0.9 }}>
          Something broke <span style={{ color: 'var(--red)' }}>off-screen</span>
        </h1>
        <p style={{ color: 'var(--muted)', maxWidth: 460, margin: '20px auto 34px', lineHeight: 1.6 }}>
          The reel snapped mid-scene. Run it back — the projector just needs a reset.
        </p>
        <button onClick={reset} className="btn-red">
          ↻ Replay the scene
        </button>
        {error.digest && (
          <p style={{ marginTop: 24, fontFamily: 'var(--mono)', fontSize: 10, letterSpacing: '0.2em', color: 'var(--dim)' }}>
            REF: {error.digest}
          </p>
        )}
      </div>
    </main>
  );
}
