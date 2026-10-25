// 把 famous-add/*.json 的佳句并入 famous-source.json 并重建 assets/famous.json
// 用法: node merge-famous.js
const fs = require('fs');
const path = require('path');
const WS = __dirname;
const SRC = path.join(WS, 'famous-source.json');

const root = JSON.parse(fs.readFileSync(SRC, 'utf8'));
const existing = new Set(root.lines.map(l => l.line));
let added = 0, dup = 0;

const addDir = path.join(WS, 'famous-add');
if (fs.existsSync(addDir)) {
    for (const f of fs.readdirSync(addDir).filter(f => f.endsWith('.json')).sort()) {
        try {
            const j = JSON.parse(fs.readFileSync(path.join(addDir, f), 'utf8'));
            for (const l of (j.lines || [])) {
                if (!l.a || !l.t || !l.line || !l.ap) continue;
                if (existing.has(l.line)) { dup++; continue; }
                existing.add(l.line);
                root.lines.push(l);
                added++;
            }
        } catch (e) { console.log('读取失败', f, e.message); }
    }
}
fs.writeFileSync(SRC, JSON.stringify(root, null, 1));
console.log(`佳句库: 新增 ${added} 条，重复跳过 ${dup} 条，总计 ${root.lines.length} 条`);
