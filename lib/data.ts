export const PROFILE = {
  name: 'MKR Infinity',
  realName: 'Mohammad Kaif Raja',
  first: 'MOHAMMAD',
  last: 'KAIF RAJA',
  title: 'MKR',
  subtitle: 'INFINITY',
  role: 'Self-Taught System Builder',
  summary:
    'A self-taught system builder from India creating practical Android tools, web products, automation workflows, and AI-powered solutions.',
  github: 'https://github.com/mkr-infinity',
  website: 'https://iamakif.me',
  instagram: 'https://www.instagram.com/mkr_infinity/',
  telegram: 'https://t.me/mkr_infinity',
  resume: 'https://iamakif.me',
};

export type ProfileId = 'developer' | 'android' | 'builder' | 'browsing';
export const VIEWERS: { id: ProfileId; name: string; color: string; icon: 'eye' | 'kunai' | 'leaf' | 'shuriken'; line: string }[] = [
  { id: 'developer', name: 'Developer', color: '#d5b15a', icon: 'kunai', line: 'Explore practical tools, guides, and the source behind every project.' },
  { id: 'android', name: 'Android tinkerer', color: '#8e9bff', icon: 'eye', line: 'From modding to ADB, bootloaders, GSIs, and apps that make Android useful.' },
  { id: 'builder', name: 'Builder', color: '#74c69d', icon: 'shuriken', line: 'Ideas become working products through systems thinking and fast experiments.' },
  { id: 'browsing', name: 'Just browsing', color: '#f2a97f', icon: 'leaf', line: 'Take a look around. Everything here is built to be useful.' },
];

export const SUBTITLES: { from: number; to: number; text: string; sfx?: boolean }[] = [
  { from: 0.05, to: 0.22, text: 'I started by breaking Android systems apart.' },
  { from: 0.22, to: 0.38, text: 'Then I learned how to rebuild them better.' },
  { from: 0.38, to: 0.55, text: 'Tools, guides, workflows, and useful experiments.' },
  { from: 0.55, to: 0.72, text: 'These days… I ship practical products.' },
  { from: 0.72, to: 0.905, text: 'Self-taught system builder. Since 2016.' },
];

export const STATS = [
  { value: 9, suffix: '+', label: 'Years building' },
  { value: 2016, suffix: '', label: 'Started in' },
  { value: 10, suffix: '', label: 'Featured projects' },
  { value: 1, suffix: '', label: 'Curious mind' },
];

export const TOP10 = [
  { name: 'Android', genre: 'Systems', hue: 145 },
  { name: 'TypeScript', genre: 'Language', hue: 215 },
  { name: 'Kotlin', genre: 'Android', hue: 270 },
  { name: 'JavaScript', genre: 'Language', hue: 48 },
  { name: 'Python', genre: 'Automation', hue: 195 },
  { name: 'React', genre: 'Frontend', hue: 195 },
  { name: 'AI workflows', genre: 'Product', hue: 330 },
  { name: 'Git', genre: 'Tools', hue: 20 },
  { name: 'ADB / Fastboot', genre: 'Android', hue: 140 },
  { name: 'Web tooling', genre: 'Product', hue: 35 },
];

export const SKILL_GENRES = [
  { name: 'Systems', items: ['Android internals', 'ADB', 'Fastboot', 'Bootloaders', 'GSI flashing'] },
  { name: 'Web tools', items: ['TypeScript', 'JavaScript', 'React', 'HTML', 'CSS'] },
  { name: 'Automation', items: ['Python', 'Workflows', 'GitHub', 'No-code', 'CLI tools'] },
  { name: 'AI products', items: ['AI workflows', 'Prompt design', 'AI-assisted builds', 'Product experiments'] },
  { name: 'Mobile', items: ['Kotlin', 'Android apps', 'Material Design', 'Offline-first tools'] },
  { name: 'Foundations', items: ['Problem solving', 'System thinking', 'Testing', 'Documentation', 'Learning by doing'] },
];

type Episode = { id: string; ep: number; name: string; tagline: string; still: string; tint: string; rank: string; match: number; url: string; liveUrl?: string; desc: string; genres: string[]; tech: string[] };

export const EPISODES: Episode[] = [
  { id: 'mkr-music', ep: 1, name: 'MKR Music', tagline: 'A calmer way to discover and listen.', still: '/stills/masked.jpg', tint: '#d5b15a', rank: 'BUILD 01', match: 100, url: 'https://github.com/mkr-infinity/mkr-music', liveUrl: 'https://iamkaif.me/mkr-music-web/', desc: 'A refined Material Design YouTube Music client for Android, crafted for effortless discovery and an immersive listening experience.', genres: ['Android', 'Music', 'Material'], tech: ['Kotlin', 'Android', 'Material 3', 'YouTube Music'] },
  { id: 'tailor-system', ep: 2, name: 'Tailor System', tagline: 'From measurements to a finished order.', still: '/stills/face.jpg', tint: '#8e9bff', rank: 'BUILD 02', match: 100, url: 'https://github.com/mkr-infinity/tailor-system', liveUrl: 'https://tailorsystem.netlify.app/', desc: 'A modern tailor management system with a public customer website and secure dashboard for clients, measurements, orders, and professional billing.', genres: ['Web app', 'Business', 'Workflow'], tech: ['Web', 'Dashboard', 'Client management', 'Billing'] },
  { id: 'revision-master', ep: 3, name: 'Revision Master', tagline: 'Turn study time into a repeatable system.', still: '/stills/kunai.jpg', tint: '#74c69d', rank: 'BUILD 03', match: 100, url: 'https://github.com/mkr-infinity/Revision-Master', liveUrl: 'http://iamkaif.me/Revision-Master/', desc: 'A smart, offline-first study app with AI-generated flashcards, mock tests, and a focus timer.', genres: ['Study', 'Offline-first', 'AI'], tech: ['TypeScript', 'AI', 'Flashcards', 'Focus timer'] },
  { id: 'matrix-calculator', ep: 4, name: 'Matrix Calculator', tagline: 'A focused toolkit for mathematics students.', still: '/stills/pull.jpg', tint: '#f2a97f', rank: 'BUILD 04', match: 100, url: 'https://github.com/mkr-infinity/Matrix_Calculator', liveUrl: 'https://buymeacoffee.com/mkr_infinity', desc: 'A lightweight matrix calculator with a broad set of features for mathematics students.', genres: ['Maths', 'Calculator', 'Education'], tech: ['TypeScript', 'Matrix calculations', 'Solvers'] },
  { id: 'windows-activation', ep: 5, name: 'Windows Activation Guide', tagline: 'A practical reference for Windows activation.', still: '/stills/vest.jpg', tint: '#8e9bff', rank: 'GUIDE 05', match: 100, url: 'https://github.com/mkr-infinity/Windows-Activation-free', desc: 'A guide and script for activating Windows 10/11 using KMS or automatic activation methods.', genres: ['Windows', 'Guide', 'Utility'], tech: ['Windows 10', 'Windows 11', 'KMS'] },
  { id: 'bootloader', ep: 6, name: 'Bootloader Unlock Guide', tagline: 'Understand the path before you unlock.', still: '/stills/hand.jpg', tint: '#d5b15a', rank: 'GUIDE 06', match: 100, url: 'https://github.com/mkr-infinity/Guide-to-unlock-Bootloader', liveUrl: 'https://bootloader-unlock.vercel.app', desc: 'A beginner-friendly guide to unlock the bootloader of an Android device, including the Lenovo TB-X306X example.', genres: ['Android', 'Bootloader', 'Guide'], tech: ['HTML', 'Fastboot', 'Android'] },
  { id: 'gsi', ep: 7, name: 'GSI Flashing Guide', tagline: 'A careful route to a compatible GSI.', still: '/stills/face.jpg', tint: '#74c69d', rank: 'GUIDE 07', match: 100, url: 'https://github.com/mkr-infinity/Guide-for-flashing-GSI-to-any-device', liveUrl: 'http://mkr-infinity.github.io/Guide-for-flashing-GSI-to-any-device/', desc: 'A fully explained guide to flash a compatible Generic System Image to an Android device.', genres: ['Android', 'Custom ROM', 'Guide'], tech: ['TypeScript', 'GSI', 'Custom ROM', 'Flashing'] },
  { id: 'adb', ep: 8, name: 'Android Command Cheatsheet', tagline: 'The commands you reach for, organised.', still: '/stills/kunai.jpg', tint: '#f2a97f', rank: 'TOOL 08', match: 100, url: 'https://github.com/mkr-infinity/android-command-cheatsheet', liveUrl: 'http://iamkaif.me/android-command-cheatsheet/', desc: 'A searchable reference for Android ADB and Fastboot commands with explanations, examples, categories, and quick-copy support.', genres: ['Android', 'Reference', 'Commands'], tech: ['TypeScript', 'ADB', 'Fastboot', 'Search'] },
  { id: 'readmehub', ep: 9, name: 'ReadmeHub', tagline: 'Make the first impression count.', still: '/stills/pull.jpg', tint: '#d5b15a', rank: 'TOOL 09', match: 100, url: 'https://github.com/mkr-infinity/readme-hub', liveUrl: 'http://iamkaif.me/readme-hub/', desc: 'A hub for creating professional GitHub READMEs automatically or by hand, with profile tools, badges, and inspiration.', genres: ['GitHub', 'Generator', 'Tool'], tech: ['TypeScript', 'GitHub', 'README', 'Generator'] },
  { id: 'calcverse', ep: 10, name: 'CalcVerse', tagline: 'Many kinds of calculation, one place.', still: '/stills/vest.jpg', tint: '#8e9bff', rank: 'BUILD 10', match: 100, url: 'https://github.com/mkr-infinity/CalcVerse', liveUrl: 'http://iamkaif.me/CalcVerse/', desc: 'A modern mathematics toolkit bringing calculations, scientific problem-solving, graphing, matrices, equations, programming tools, and unit conversion together.', genres: ['Maths', 'Science', 'Toolkit'], tech: ['TypeScript', 'Graphing', 'Matrices', 'Unit conversion'] },
];

export const AWARDS = [
  { title: 'Builder', sub: 'Self-taught since 2016', laurel: 'SYSTEMS & TOOLS' },
  { title: 'Open source', sub: 'Practical projects shipped', laurel: 'BUILD · LEARN · SHARE' },
  { title: 'Problem solver', sub: 'Android, web, automation, AI', laurel: 'USEFUL BY DESIGN' },
];
export const CERTS = [
  { title: 'Android tooling', by: 'Kotlin · ADB · Termux' },
  { title: 'Web products', by: 'React · TypeScript · APIs' },
  { title: 'Automation', by: 'n8n · Make · No-code systems' },
  { title: 'AI workflows', by: 'Agents · RAG · Prompt systems' },
];
export const SEASONS = [
  { label: '2016', name: 'The Android Modding Arc', years: '2016 → now', episodes: [{ title: 'Learning by experimenting', where: 'India', note: 'Self-taught systems work, tools, and practical products.', still: '/stills/pull.jpg' }] },
];
