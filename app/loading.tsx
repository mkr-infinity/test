export default function Loading() {
  return (
    <div
      style={{
        minHeight: '100svh',
        display: 'grid',
        placeItems: 'center',
        background: '#000',
        color: 'var(--muted)',
        fontFamily: 'var(--mono)',
        fontSize: 12,
        letterSpacing: '0.4em',
        textTransform: 'uppercase',
      }}
    >
      Loading the episode…
    </div>
  );
}
