// 把 fill-done/*.json 中的背景（及可选译文）合并回 bulk 分块文件并同步到 assets
// 用法: node fill-backgrounds.js [fill-done/roundXX.json]
const fs = require('fs');
const path = require('path');
const BULK = path.join(__dirname, 'poems-bulk');
const ASSETS = path.join(__dirname, '..', 'shici', 'app', 'src', 'main', 'assets', 'bulk');

const fileArg = process.argv[2];
const fillFiles = fileArg
    ? [path.resolve(fileArg)]
    : fs.readdirSync(path.join(__dirname, 'fill-done')).filter(f => f.endsWith('.json')).map(f => path.join(__dirname, 'fill-done', f));

const fills = new Map();
for (const fp of fillFiles) {
    try {
        const j = JSON.parse(fs.readFileSync(fp, 'utf8'));
        for (const it of (j.fills || [])) {
            if (it.id && it.background) fills.set(it.id, it);
        }
    } catch (e) {
        console.log('读取失败', fp, e.message);
    }
}
if (fills.size === 0) { console.log('无有效补全条目'); process.exit(0); }

let patched = 0;
for (const f of fs.readdirSync(BULK).filter(f => f.startsWith('bulk_')).sort()) {
    const fp = path.join(BULK, f);
    let changed = false;
    const root = JSON.parse(fs.readFileSync(fp, 'utf8'));
    for (const p of root.poems) {
        const fill = fills.get(p.id);
        if (fill && !p.background) {
            p.background = fill.background;
            if (fill.translation && !p.translation) p.translation = fill.translation;
            if (Array.isArray(fill.famous) && fill.famous.length && !p.famous) p.famous = fill.famous;
            fills.delete(p.id);
            patched++;
            changed = true;
        }
    }
    if (changed) fs.writeFileSync(fp, JSON.stringify(root));
}
console.log(`已补全 ${patched} 首，未匹配 ${fills.size} 条`);
if (fills.size) console.log([...fills.keys()].slice(0, 10).join(','));
fs.mkdirSync(ASSETS, { recursive: true });
for (const f of fs.readdirSync(BULK).filter(f => f.startsWith('bulk_'))) {
    fs.copyFileSync(path.join(BULK, f), path.join(ASSETS, f));
}
console.log('assets/bulk 已同步');
