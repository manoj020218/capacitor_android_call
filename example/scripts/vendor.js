// Copies the plugin's built ESM output (and @capacitor/core's) into www/vendor/
// so the harness can use plain browser ES module imports + an import map,
// with no bundler step. Re-run after `npm run build` in the plugin root
// whenever plugin source changes.
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const pluginEsm = path.join(root, '..', 'dist', 'esm');
const capacitorCoreEsm = path.join(root, 'node_modules', '@capacitor', 'core', 'dist', 'index.js');
const vendorDir = path.join(root, 'www', 'vendor');

fs.mkdirSync(path.join(vendorDir, 'capacitor-native-call'), { recursive: true });
fs.mkdirSync(path.join(vendorDir, 'capacitor-core'), { recursive: true });

for (const file of ['index.js', 'web.js', 'definitions.js']) {
  const src = path.join(pluginEsm, file);
  let content = fs.readFileSync(src, 'utf8');
  // Browser ESM requires explicit extensions on relative specifiers.
  content = content.replace(/from '(\.\/[^']+)'/g, (_m, spec) => `from '${spec}.js'`);
  content = content.replace(/import\('(\.\/[^']+)'\)/g, (_m, spec) => `import('${spec}.js')`);
  fs.writeFileSync(path.join(vendorDir, 'capacitor-native-call', file), content);
}

fs.copyFileSync(capacitorCoreEsm, path.join(vendorDir, 'capacitor-core', 'index.js'));

console.log('Vendored plugin + @capacitor/core ESM into www/vendor/');
