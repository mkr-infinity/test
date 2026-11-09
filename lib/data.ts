export const PROFILE = {
  name: 'Mohammad Kaif Raja',
  realName: 'Kaif Raja',
  first: 'KAIF',
  last: 'INFINITY',
  title: 'KAKASHI',
  subtitle: 'UNMASKED',
  role: 'System Optimizer & Open-Source Builder',
  summary:
    'Passionate about systems, not just code. From flashing operating systems to customizing Linux, I have tested countless distros. Android modder, Linux tinkerer and open-source builder — tech is my playground, curiosity my guide.',
  email: 'mohammadkaifraja2@gmail.com',
  github: 'https://github.com/mkr-infinity',
  linkedin: 'https://t.me/mkr_infinity',
  instagram: 'https://www.instagram.com/mkr_infinity',
  resume: 'https://github.com/mkr-infinity?tab=repositories',
};

export type ProfileId = 'recruiter' | 'developer' | 'hackathon' | 'browsing';
export const VIEWERS: { id: ProfileId; name: string; color: string; icon: 'eye' | 'kunai' | 'leaf' | 'shuriken'; line: string }[] = [
  { id: 'recruiter', name: 'Recruiter', color: '#e50914', icon: 'eye', line: 'Looking for someone who can build, break and fix anything? You picked the right show.' },
  { id: 'developer', name: 'Developer', color: '#2f6fed', icon: 'kunai', line: 'Fellow builder — the source code of every mission lives on GitHub.' },
  { id: 'hackathon', name: 'Hackathon Squad', color: '#f5b301', icon: 'shuriken', line: 'Need a teammate who ships at 3 a.m.? So do I. Crafted by curiosity, perfected by persistence.' },
  { id: 'browsing', name: 'Just Browsing', color: '#1db954', icon: 'leaf', line: 'Grab some popcorn. The mask comes off in a few scrolls.' },
];

// Subtitles shown while the hero "plays" (keyed by hero scroll progress)
export const SUBTITLES: { from: number; to: number; text: string; sfx?: boolean }[] = [
  { from: 0.05, to: 0.22, text: 'Everyone keeps asking what’s under the mask.' },
  { from: 0.22, to: 0.38, text: 'Fine. Just this once.' },
  { from: 0.38, to: 0.55, text: 'No more distros. No more flashing. Okay, maybe one more.' },
  { from: 0.55, to: 0.72, text: 'These days… I build in the open.' },
  { from: 0.72, to: 0.905, text: 'System Optimizer & Open-Source Builder. Shall we begin?' },
];

export const STATS = [
  { value: 30, suffix: '+', label: 'Public Repos' },
  { value: 428, suffix: '+', label: 'GitHub Stars' },
  { value: 68, suffix: '⭐', label: 'Top Repo' },
  { value: 6, suffix: '+', label: 'Live Tools' },
];

export const TOP10 = [
  { name: 'Linux', genre: 'Systems', hue: 205 },
  { name: 'Android Modding', genre: 'Systems', hue: 140 },
  { name: 'Python', genre: 'Language', hue: 48 },
  { name: 'TypeScript', genre: 'Language', hue: 215 },
  { name: 'Kotlin', genre: 'Language', hue: 190 },
  { name: 'Bash', genre: 'Tooling', hue: 80 },
  { name: 'React.js', genre: 'Frontend', hue: 195 },
  { name: 'Git', genre: 'Tooling', hue: 12 },
  { name: 'Docker', genre: 'DevOps', hue: 200 },
  { name: 'ADB & Fastboot', genre: 'Android', hue: 0 },
];

export const SKILL_GENRES = [
  { name: 'Systems', items: ['Arch Linux', 'Debian', 'Ubuntu', 'Kali', 'BSPWM', 'Bash'] },
  { name: 'Android', items: ['ADB & Fastboot', 'GSI Flashing', 'Bootloader Unlock', 'Kernel Tweaks', 'ROM Modding'] },
  { name: 'Languages', items: ['Python', 'TypeScript', 'JavaScript', 'Kotlin', 'Java'] },
  { name: 'Web', items: ['React.js', 'Next.js', 'HTML', 'CSS', 'Firebase'] },
  { name: 'Tools', items: ['Git', 'Docker', 'GitHub Actions', 'No-Code Tools', 'Automation'] },
  { name: 'Core', items: ['System Optimization', 'FOSS', 'Open Source', 'Troubleshooting', 'Performance Tuning'] },
];

export const EPISODES = [
  {
    id: 'gsi-guide',
    ep: 1,
    name: 'GSI Flashing Guide',
    tagline: 'The brick never stood a chance.',
    still: '/stills/masked.jpg',
    tint: '#e50914',
    rank: 'S-Rank',
    match: 99,
    url: 'https://github.com/mkr-infinity/Guide-for-flashing-GSI-to-any-device',
    desc: 'The fully explained guide to flashing a compatible Generic System Image to any Android device — live at mkr-infinity.github.io, with a companion bootloader-unlock guide for the full modding journey.',
    genres: ['Android', 'Guide', 'Open Source'],
    tech: ['ADB', 'Fastboot', 'GSI', 'Android', 'Linux', 'Documentation'],
  },
  {
    id: 'revision-master',
    ep: 2,
    name: 'Revision Master',
    tagline: 'Every revision counts.',
    still: '/stills/face.jpg',
    tint: '#2f6fed',
    rank: 'S-Rank',
    match: 97,
    url: 'https://github.com/mkr-infinity/Revision-Master',
    desc: 'A smart, offline-first study companion that turns revision into structured practice — flashcards, mock tests and AI-generated study tools to keep you on track.',
    genres: ['Study', 'Productivity', 'AI'],
    tech: ['TypeScript', 'React', 'AI', 'Offline-First', 'Flashcards', 'Mock Tests'],
  },
  {
    id: 'mkr-music',
    ep: 3,
    name: 'MKR Music',
    tagline: 'Your music. Your way.',
    still: '/stills/kunai.jpg',
    tint: '#f5b301',
    rank: 'S-Rank',
    match: 96,
    url: 'https://github.com/mkr-infinity/mkr-music',
    desc: 'A refined, Material Design YouTube Music client for Android — multiple sources brought together in one clean, ad-free listening experience.',
    genres: ['Android', 'Music', 'Material Design'],
    tech: ['Kotlin', 'Material Design', 'Android', 'YouTube Music', 'Jetpack'],
  },
  {
    id: 'readme-hub',
    ep: 4,
    name: 'ReadmeHub',
    tagline: 'READMEs that write themselves.',
    still: '/stills/calm.jpg',
    tint: '#1db954',
    rank: 'S-Rank',
    match: 95,
    url: 'https://github.com/mkr-infinity/readme-hub',
    desc: 'The ultimate hub for README creation — seamlessly automatic or fully hand-crafted, for repositories and GitHub profiles alike.',
    genres: ['Developer Tools', 'Automation', 'Open Source'],
    tech: ['TypeScript', 'React', 'Markdown', 'Automation', 'GitHub'],
  },
  {
    id: 'calcverse',
    ep: 5,
    name: 'CalcVerse',
    tagline: 'Every calculation, one universe.',
    still: '/stills/pull.jpg',
    tint: '#2f6fed',
    rank: 'S-Rank',
    match: 94,
    url: 'https://github.com/mkr-infinity/CalcVerse',
    desc: 'A modern mathematics toolkit bringing calculations, graphing, matrices and conversions together in one clean, fast interface.',
    genres: ['Math', 'Web App', 'Education'],
    tech: ['TypeScript', 'React', 'Graphing', 'Math', 'Conversions'],
  },
  {
    id: 'solo-ledger',
    ep: 6,
    name: 'Solo-Ledger',
    tagline: 'Your money. Offline. Always.',
    still: '/stills/vest.jpg',
    tint: '#e50914',
    rank: 'A-Rank',
    match: 93,
    url: 'https://github.com/mkr-infinity/Solo-Ledger',
    desc: 'A very lightweight, offline-first budget tracker for Android — private by design and easy by default, with no account required.',
    genres: ['Android', 'Finance', 'Offline-First'],
    tech: ['Kotlin', 'Android', 'Offline-First', 'Material Design'],
  },
];

export const AWARDS = [
  { laurel: 'MOST STARRED', title: '68 ⭐ · GSI Flashing Guide', sub: 'Guide-for-flashing-GSI-to-any-device' },
  { laurel: 'CROWD FAVORITE', title: '33 ⭐ · Revision Master', sub: 'Smart offline-first study app' },
  { laurel: 'OPEN SOURCE RECORD', title: '428+ Stars · 30 Repos', sub: 'Built with curiosity, shared with everyone' },
];

export const CERTS = [
  { by: 'Live Tool', title: 'CalcVerse — Math Toolkit', hue: 215 },
  { by: 'Live Tool', title: 'ReadmeHub — README Generator', hue: 270 },
  { by: 'Live Demo', title: 'Tailor Management System', hue: 140 },
  { by: 'Guide', title: 'Bootloader Unlock Guide', hue: 20 },
  { by: 'Python', title: 'Attendance System', hue: 50 },
  { by: 'Kotlin', title: 'Solo-Ledger — Budget Tracker', hue: 0 },
  { by: 'Python', title: 'TeleAutomator — AI Automation', hue: 45 },
  { by: 'TypeScript', title: 'MIRA — Responsive Assistant', hue: 205 },
];

export const SEASONS = [
  {
    label: 'Season 1',
    name: 'The Distro-Hopping Arc',
    years: 'The Early Days',
    episodes: [{ title: 'Flashing & Breaking', where: 'Every distro I could get my hands on', note: 'Arch · Debian · Ubuntu · Kali', still: '/stills/pull.jpg' }],
  },
  {
    label: 'Season 2',
    name: 'The Android Arc',
    years: 'The Modding Years',
    episodes: [{ title: 'Bootloaders & GSIs', where: 'Countless devices, countless bricks — all worth it', note: '68⭐ GSI flashing guide · rooted-device tricks', still: '/stills/vest.jpg' }],
  },
  {
    label: 'Season 3',
    name: 'The Builder Arc',
    years: 'Now',
    episodes: [
      { title: 'Open Source, Every Day', where: 'GitHub · @mkr-infinity', note: 'Tools, apps and guides — crafted by curiosity, perfected by persistence.', still: '/stills/hand.jpg' },
    ],
  },
];

export const GUIDES = [
  {
    id: 'gsi',
    name: 'GSI Flashing Guide',
    stars: 68,
    tagline: 'The fully explained guide to flashing a compatible Generic System Image to any device.',
    url: 'https://github.com/mkr-infinity/Guide-for-flashing-GSI-to-any-device',
    live: 'https://mkr-infinity.github.io/Guide-for-flashing-GSI-to-any-device/',
  },
  {
    id: 'bootloader',
    name: 'Bootloader Unlock Guide',
    stars: 27,
    tagline: 'Beginner-friendly walkthrough for unlocking the bootloader on any Android device.',
    url: 'https://github.com/mkr-infinity/Guide-to-unlock-Bootloader',
    live: 'https://bootloader-unlock.vercel.app/',
  },
  {
    id: 'drivers',
    name: 'ADB & Fastboot Drivers',
    stars: 13,
    tagline: 'Official Google drivers — always installed and updated, no more searching.',
    url: 'https://github.com/mkr-infinity/ADB-Fastboot_Drivers',
  },
  {
    id: 'cheatsheet',
    name: 'Android Command CheatSheet',
    stars: 8,
    tagline: 'A beautiful, searchable reference for ADB & Fastboot commands — live tool included.',
    url: 'https://github.com/mkr-infinity/android-command-cheatsheet',
    live: 'https://mkr-infinity.github.io/android-command-cheatsheet/',
  },
  {
    id: 'phonepe',
    name: 'PhonePe on Rooted Devices',
    stars: 12,
    tagline: 'A tested method to run PhonePe on rooted devices without breaking the bank app.',
    url: 'https://github.com/mkr-infinity/PhonePe-in-rooted-device',
  },
];
