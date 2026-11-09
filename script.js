/* MKR portfolio v2 — theme toggle + scroll reveal. No frameworks. */
(function () {
  'use strict';

  var root = document.documentElement;

  // ---- theme toggle (data-theme set pre-paint by the inline head script) ----
  var toggle = document.getElementById('theme-toggle');
  if (toggle) {
    toggle.addEventListener('click', function () {
      var next = root.dataset.theme === 'light' ? 'dark' : 'light';
      root.dataset.theme = next;
      try { localStorage.setItem('theme', next); } catch (e) { /* private mode */ }
    });
  }

  // ---- footer year ----
  var year = document.getElementById('year');
  if (year) year.textContent = String(new Date().getFullYear());

  // ---- reveal on scroll ----
  var els = Array.prototype.slice.call(document.querySelectorAll('.reveal'));
  var reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!reduced && 'IntersectionObserver' in window && els.length) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('in');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
    els.forEach(function (el) { io.observe(el); });
  } else {
    els.forEach(function (el) { el.classList.add('in'); });
  }

  // ---- stagger: siblings of the same reveal group get small delays ----
  document.querySelectorAll('.grid, .guides, .timeline, .stats').forEach(function (group) {
    Array.prototype.forEach.call(group.children, function (child, i) {
      child.style.transitionDelay = Math.min(i * 70, 420) + 'ms';
    });
  });
})();
