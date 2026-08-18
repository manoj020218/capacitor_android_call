import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const root = process.cwd();
const roots = [
  'cap-core',
  'cap-device-health',
  'cap-location',
  'cap-lifecycle',
  'cap-push',
  'cap-dialer',
  'cap-device-policy',
];
const failures = [];

function walk(dir) {
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    const stat = statSync(full);
    if (stat.isDirectory()) {
      if (['dist', 'node_modules', 'build'].includes(entry)) continue;
      walk(full);
      continue;
    }
    if (!/\.(ts|tsx|kt|xml|gradle|md|mjs)$/.test(entry)) continue;
    const text = readFileSync(full, 'utf8');
    const lines = text.split(/\r?\n/).length;
    if (lines > 200) failures.push({ full, lines });
  }
}

for (const name of roots) {
  walk(join(root, name));
}

if (failures.length) {
  for (const failure of failures) {
    console.error(`${failure.lines} lines: ${failure.full}`);
  }
  process.exit(1);
}

console.log('All handwritten files are at or under 200 lines.');
