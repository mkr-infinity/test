'use client';

import { motion } from 'framer-motion';
import { GUIDES } from '@/lib/data';

export default function Guides() {
  return (
    <section className="guides" id="guides">
      <header className="sec-head center">
        <p className="eyebrow">SIDE QUESTS</p>
        <h2>
          Field <span>guides</span>
        </h2>
      </header>
      <div className="gd-grid">
        {GUIDES.map((g, i) => (
          <motion.a
            key={g.id}
            href={g.url}
            target="_blank"
            rel="noreferrer"
            className="gd-card"
            initial={{ opacity: 0, y: 40 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ delay: i * 0.08, duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
          >
            <span className="gd-stars">★ {g.stars}</span>
            <h3>{g.name}</h3>
            <p>{g.tagline}</p>
            <span className="gd-link">Read the guide ↗</span>
          </motion.a>
        ))}
      </div>
    </section>
  );
}
