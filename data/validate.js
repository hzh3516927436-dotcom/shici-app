// 校验 data/poems/*.json 是否符合规范，并输出统计
const fs = require('fs');
const path = require('path');

const DIR = path.join(__dirname, 'poems');
const dynasties = ['先秦','汉','三国','魏晋','南北朝','隋','唐','五代','宋','元','明','清','近代'];
const forms = ['四言诗','楚辞体','乐府诗','五言古诗','七言古诗','五言绝句','七言绝句','五言律诗','七言律诗','词','散曲','辞赋','骈文','散文'];
const themes = ['山水田园','边塞征戍','咏物言志','咏史怀古','送别怀人','羁旅思乡','闺怨宫怨','悼亡伤逝','讽喻刺世','爱国忧时','干谒酬赠','题画题壁','闲适隐逸','游仙梦幻','宫体艳情','哲理玄言','节令风俗','童趣生活','写景抒情'];
// 类目为开放集：未在推荐清单中的题材仅提示，不判错
const textbooks = ['小学必背','初中必背','高中必背'];

const errors = [];
const warnings = [];
const ids = new Set();
const titles = new Set();
const poets = new Map();
let total = 0;

const files = fs.existsSync(DIR) ? fs.readdirSync(DIR).filter(f => f.endsWith('.json')) : [];
if (files.length === 0) {
    console.log('NO DATA FILES YET');
    process.exit(0);
}
for (const f of files) {
    let root;
    try {
        root = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8'));
    } catch (e) {
        errors.push(`${f}: JSON 解析失败 - ${e.message}`);
        continue;
    }
    const poems = root.poems || [];
    for (let i = 0; i < poems.length; i++) {
        const p = poems[i];
        const tag = `${f}[${i}] ${p.id || '无id'} 《${p.title || '?'}》`;
        total++;
        if (!p.id) errors.push(`${tag}: 缺 id`);
        else if (ids.has(p.id)) errors.push(`${tag}: id 重复`);
        else ids.add(p.id);
        if (!p.title) errors.push(`${tag}: 缺 title`);
        const key = p.title + '|' + p.author;
        if (titles.has(key)) warnings.push(`${tag}: 疑似重复篇目（同题同作者）`);
        else titles.add(key);
        if (!p.author) errors.push(`${tag}: 缺 author`);
        if (!dynasties.includes(p.dynasty)) errors.push(`${tag}: dynasty 非法值 "${p.dynasty}"`);
        if (!forms.includes(p.form)) errors.push(`${tag}: form 非法值 "${p.form}"`);
        if ((p.form === '词' || p.form === '散曲') && !p.tune) warnings.push(`${tag}: 词/散曲缺 tune`);
        if (!Array.isArray(p.themes) || p.themes.length === 0) errors.push(`${tag}: 缺 themes`);
        else p.themes.forEach(t => { if (!themes.includes(t)) warnings.push(`${tag}: 非推荐类目 "${t}"（开放集，仅提示）`); });
        if (!p.body || p.body.length < 8) errors.push(`${tag}: body 过短或缺失`);
        if (!p.translation || p.translation.length < 10) errors.push(`${tag}: 缺 translation`);
        if (!Array.isArray(p.notes) || p.notes.length < 2) warnings.push(`${tag}: notes 少于 2 条`);
        else p.notes.forEach(n => { if (!n.w || !n.n) errors.push(`${tag}: notes 条目缺 w/n`); });
        if (!p.background || p.background.length < 100) warnings.push(`${tag}: background 过短(${(p.background||'').length}字)`);
        if (p.famous && !Array.isArray(p.famous)) errors.push(`${tag}: famous 非数组`);
        if (p.textbook && !textbooks.includes(p.textbook)) errors.push(`${tag}: textbook 非法值 "${p.textbook}"`);
    }
    for (const poet of (root.poets || [])) {
        if (!poet.name) { errors.push(`${f}: poet 缺 name`); continue; }
        if (!poets.has(poet.name)) poets.set(poet.name, { ...poet, file: f });
    }
}

// 朝代分布
const dist = {};
for (const f of files) {
    try {
        const root = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8'));
        (root.poems || []).forEach(p => { dist[p.dynasty] = (dist[p.dynasty] || 0) + 1; });
    } catch (e) { /* 已报错 */ }
}

console.log('===== 统计 =====');
console.log(`文件数: ${files.length}, 诗词总数: ${total}, 诗人数: ${poets.size}`);
console.log('朝代分布:', dynasties.filter(d => dist[d]).map(d => `${d}${dist[d]}`).join(' '));
console.log(`错误: ${errors.length}, 警告: ${warnings.length}`);
if (errors.length) {
    console.log('===== 错误 =====');
    errors.slice(0, 60).forEach(e => console.log('  ' + e));
    if (errors.length > 60) console.log(`  ……另有 ${errors.length - 60} 条`);
}
if (warnings.length) {
    console.log('===== 警告(前20) =====');
    warnings.slice(0, 20).forEach(w => console.log('  ' + w));
}
process.exit(errors.length > 0 ? 1 : 0);
