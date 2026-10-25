// 将 famous-source.json 的佳句解析到具体作品 id，输出 assets/famous.json
const fs = require('fs');
const path = require('path');
const WS = __dirname;

const src = JSON.parse(fs.readFileSync(path.join(WS, 'famous-source.json'), 'utf8')).lines;

// 建立索引：作者 → [{title, id, isCurated}]
const index = [];
function addIndex(dir, isCurated) {
    if (!fs.existsSync(dir)) return;
    for (const f of fs.readdirSync(dir).filter(f => f.endsWith('.json'))) {
        try {
            const root = JSON.parse(fs.readFileSync(path.join(dir, f), 'utf8'));
            for (const p of (root.poems || [])) index.push({ author: p.author, title: p.title, id: p.id, dynasty: p.dynasty, body: p.body, isCurated });
        } catch (e) { }
    }
}
addIndex(path.join(WS, 'poems'), true);
addIndex(path.join(WS, 'poems-bulk'), false);

const norm = s => (s || '').replace(/[（(].*?[)）]/g, '').replace(/[·・\s]/g, '').split('其')[0];
// 作者别名：语料库中诗经/乐府署名统一为"无名氏"
const A_ALIAS = { '诗经': '无名氏', '汉乐府': '无名氏', '乐府': '无名氏', '佚名': '无名氏', '北朝民歌': '无名氏', '南朝民歌': '无名氏', '北朝乐府': '无名氏', '南朝乐府': '无名氏' };

const out = [];
const unmatched = [];
for (let i = 0; i < src.length; i++) {
    const s = src[i];
    const key = norm(s.t);
    const author = A_ALIAS[s.a] || s.a;
    let cands = [];
    for (const k of [key, key.split('·')[0], key.split('・')[0]]) {
        if (!k) continue;
        cands = index.filter(x => x.author === author && (norm(x.title) === k || norm(x.title).includes(k)));
        if (cands.length) break;
    }
    const hit = cands.find(x => x.isCurated) || cands[0];
    if (!hit) {
        // 兜底：用佳句原文在语料正文中搜索（先试首句截断，再试全句）
        const nline = norm(s.line.split(/[，。？！；\n]/)[0]).slice(0, 10);
        const nfull = norm(s.line).slice(0, 16);
        let bodyHit = null;
        for (const probe of [nline, nfull]) {
            if (probe.length < 5) continue;
            const byBody = index.filter(x => x.author === author && norm(x.body || '').includes(probe));
            bodyHit = byBody.find(x => x.isCurated) || byBody[0];
            if (bodyHit) break;
        }
        if (bodyHit) {
            out.push({ id: 'F' + String(i + 1).padStart(3, '0'), line: s.line, title: bodyHit.title, author: s.a, dynasty: bodyHit.dynasty || '', poemId: bodyHit.id, appreciation: s.ap });
            continue;
        }
        unmatched.push(`${s.a}|${s.t}`);
        out.push({ id: 'F' + String(i + 1).padStart(3, '0'), line: s.line, title: s.t, author: s.a, dynasty: s.dynasty || '', poemId: '', appreciation: s.ap });
        continue;
    }
    out.push({
        id: 'F' + String(i + 1).padStart(3, '0'),
        line: s.line,
        title: hit.title.replace(/（节选）$/, ''),
        author: s.a,
        dynasty: hit.dynasty || s.dynasty || '',
        poemId: hit.id,
        appreciation: s.ap
    });
}

fs.writeFileSync(
    path.join(WS, '..', 'shici', 'app', 'src', 'main', 'assets', 'famous.json'),
    JSON.stringify({ lines: out }, null, 1)
);
console.log(`解析成功 ${out.length}/${src.length} 条`);
if (unmatched.length) console.log('未匹配:', unmatched.join(' , '));
