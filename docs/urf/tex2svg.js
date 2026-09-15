// Reads a JSON list of {tex, display} on stdin, prints a JSON list of SVG strings.
// Used by math2svg.lua. Needs: npm install mathjax@3   (in this folder)
const path = require('path');
require(path.join(__dirname, 'node_modules', 'mathjax')).init({
  loader: { load: ['input/tex', 'output/svg'] },
  svg: { fontCache: 'none' }          // inline glyph paths, no <use> references
}).then(async (MathJax) => {
  let input = '';
  for await (const chunk of process.stdin) input += chunk;
  const out = [];
  for (const { tex, display } of JSON.parse(input)) {
    const node = await MathJax.tex2svgPromise(tex, { display });
    out.push(MathJax.startup.adaptor.innerHTML(node));   // just the <svg>...</svg>
  }
  process.stdout.write(JSON.stringify(out));
}).catch((err) => { console.error(err); process.exit(1); });
