const CACHE = 'kbbi-shell-v3';
const ICONS = ['default', 'royal_ocean', 'golden_sunset', 'golden_coral_energy', 'deep_forest_energy', 'neon_violet', 'blaze_orange'];
const SHELL = ['./', './index.html', './styles.css', './manifest.webmanifest', './icon-maskable-512.png', ...ICONS.map(name => `./icon-${name}.png`)];
self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(SHELL)));
  self.skipWaiting();
});
self.addEventListener('activate', event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE).map(key => caches.delete(key)))));
  self.clients.claim();
});
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;
  const url = new URL(event.request.url);
  if (url.origin !== self.location.origin) return;
  if (event.request.mode === 'navigate') {
    event.respondWith(fetch(event.request).catch(() => caches.match('./index.html')));
  } else {
    event.respondWith(caches.match(event.request).then(cached => {
      if (cached) return cached;
      return fetch(event.request).then(response => {
        if (response.ok) caches.open(CACHE).then(cache => cache.put(event.request, response.clone()));
        return response;
      });
    }));
  }
});
