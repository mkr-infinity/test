'use client';

import { motion } from 'framer-motion';
import { useRef } from 'react';
import { PROFILE, EPISODES } from '@/lib/data';

function Magnetic({ href, children, primary }: { href: string; children: React.ReactNode; primary?: boolean }) {
  const ref = useRef<HTMLAnchorElement>(null);
  const move = (e: React.PointerEvent) => {
    const el = ref.current!;
    const b = el.getBoundingClientRect();
    const x = e.clientX - (b.left + b.width / 2);
    const y = e.clientY - (b.top + b.height / 2);
    el.style.transform = `translate(${x * 0.3}px, ${y * 0.4}px)`;
  };
  const leave = () => {
    if (ref.current) ref.current.style.transform = '';
  };
  const ext = href.startsWith('http') || href.endsWith('.pdf');
  return (
    <a ref={ref} href={href} className={`mag ${primary ? 'primary' : ''}`} onPointerMove={move} onPointerLeave={leave} target={ext ? '_blank' : undefined} rel="noreferrer">
      <span>{children}</span>
    </a>
  );
}

const ROLL = [
  ['Written & Directed by', PROFILE.realName],
  ['Lead Role', PROFILE.role],
  ['Focus', 'Android · Web · Automation · AI'],
  ['Tools', 'TypeScript · Kotlin · Python · React'],
  ['Featuring', EPISODES.map((e) => e.name).join(' · ')],
  ['Origin', 'India · Self-taught since 2016'],
  ['Home base', 'iamakif.me'],
  ['Source', 'github.com/mkr-infinity'], 
];

export default function Finale() {
  return (
    <section className="finale" id="contact">
      <div className="fin-glow" />
      <p className="eyebrow center">FINAL CHAPTER</p>
      <motion.h2 className="fin-title" initial="h" whileInView="s" viewport={{ once: true, amount: 0.3 }}>
        {['LET’S MAKE', 'THE SEQUEL'].map((l, i) => (
          <span key={l} className={`fl ${i ? 'outline' : ''}`}>
            <motion.span variants={{ h: { y: '105%' }, s: { y: '0%' } }} transition={{ delay: i * 0.12, duration: 1.1, ease: [0.16, 1, 0.3, 1] }}>
              {l}
            </motion.span>
          </span>
        ))}
      </motion.h2>
      <p className="fin-sub">Open to full-stack roles, internships and hackathon squads. The next mission starts with one message.</p>
      <div className="fin-links">
        <Magnetic href={PROFILE.website} primary>
          Visit my website ↗
        </Magnetic>
        <Magnetic href={PROFILE.github}>GitHub ↗</Magnetic>
        <Magnetic href={PROFILE.instagram}>Instagram ↗</Magnetic>
        <Magnetic href={PROFILE.telegram}>Telegram ↗</Magnetic>
      </div>

      <div className="roll" aria-label="Credits">
        <div className="roll-inner">
          {ROLL.map(([k, v]) => (
            <div className="roll-row" key={k}>
              <span>{k}</span>
              <b>{v}</b>
            </div>
          ))}
          <div className="roll-end">TO BE CONTINUED…</div>
        </div>
      </div>

      <footer className="fin-foot">
        <span>© 2026 {PROFILE.realName}</span>
        <span>Scroll up to rewind the scene ↑</span>
      </footer>
    </section>
  );
}
