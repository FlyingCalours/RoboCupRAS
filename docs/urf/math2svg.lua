-- math2svg.lua: turn every $...$ / $$...$$ into an SVG rendered by MathJax,
-- so PDF engines without JavaScript (WeasyPrint) can show real LaTeX math.
local script = pandoc.path.join{pandoc.path.directory(PANDOC_SCRIPT_FILE), 'tex2svg.js'}

function Pandoc(doc)
  -- pass 1: collect all formulas
  local items = {}
  doc:walk{ Math = function(m)
    items[#items + 1] = { tex = m.text, display = (m.mathtype == 'DisplayMath') }
  end }
  if #items == 0 then return nil end

  -- render them all with ONE node call (fast)
  local svgs = pandoc.json.decode(
    pandoc.pipe('node', {script}, pandoc.json.encode(items)), false)

  -- pass 2: replace each formula with its SVG, in the same order
  local i = 0
  return doc:walk{ Math = function(m)
    i = i + 1
    local cls = (m.mathtype == 'DisplayMath') and 'math display' or 'math inline'
    return pandoc.RawInline('html', '<span class="' .. cls .. '">' .. svgs[i] .. '</span>')
  end }
end
