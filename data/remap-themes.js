// 把旧题材类目重映射到新诗学类目，并为存量数据补充新类目标签
// 用法: node remap-themes.js [目录或文件...]  默认处理 poems/
const fs = require('fs');
const path = require('path');

const RENAME = {
    '边塞征战': '边塞征戍',
    '送别友情': '送别怀人',
    '思乡羁旅': '羁旅思乡',
    '爱情相思': '闺怨宫怨',
    '悼亡追忆': '悼亡伤逝',
    '哲理励志': '哲理玄言',
    '宴饮酬唱': '干谒酬赠',
    '忧国忧民': null // 拆分，见下
};

function splitYouGuo(title, body) {
    const s = title + ' ' + body.slice(0, 150);
    if (/民|租|税|卖|贫|饥|役|朱门|赋敛/.test(s)) return '讽喻刺世';
    return '爱国忧时';
}

function extraTags(title, body) {
    const t = title, b = body.slice(0, 120);
    const add = [];
    const has = (re) => re.test(t) || re.test(b);
    if (has(/题画|画中|题壁|书壁|壁间|画鹰|画竹|画梅|看画/)) add.push('题画题壁');
    if (has(/闲居|幽居|隐|逸|渔樵|山居|闲适|静坐|独坐|柴扉/)) add.push('闲适隐逸');
    if (has(/游仙|梦游|仙山|蓬莱|瑶台|魂梦/)) add.push('游仙梦幻');
    return add;
}

const args = process.argv.slice(2);
const targets = args.length ? args : [path.join(__dirname, 'poems')];
const NEW_THEMES = ['山水田园','边塞征戍','咏物言志','咏史怀古','送别怀人','羁旅思乡','闺怨宫怨','悼亡伤逝','讽喻刺世','爱国忧时','干谒酬赠','题画题壁','闲适隐逸','游仙梦幻','宫体艳情','哲理玄言','节令风俗','童趣生活','写景抒情'];

let changed = 0, total = 0;
for (const target of targets) {
    const files = fs.statSync(target).isDirectory()
        ? fs.readdirSync(target).filter(f => f.endsWith('.json')).map(f => path.join(target, f))
        : [target];
    for (const fp of files) {
        let root;
        try { root = JSON.parse(fs.readFileSync(fp, 'utf8')); } catch (e) { console.log('跳过(解析失败)', fp); continue; }
        let dirty = false;
        for (const p of (root.poems || [])) {
            total++;
            const nt = [];
            for (let t of (p.themes || [])) {
                if (t === '忧国忧民') t = splitYouGuo(p.title, p.body);
                else if (RENAME[t]) t = RENAME[t];
                if (t && !nt.includes(t)) nt.push(t);
            }
            for (const e of extraTags(p.title, p.body)) if (!nt.includes(e) && nt.length < 3) nt.push(e);
            // 类目为开放集：保留重映射结果与未知新类目，仅过滤空值
            const clean = nt.filter(t => t && t.trim());
            if (clean.join('|') !== (p.themes || []).join('|')) {
                p.themes = clean.length ? clean : ['写景抒情'];
                dirty = true; changed++;
            }
        }
        if (dirty) fs.writeFileSync(fp, JSON.stringify(root, null, 1));
    }
}
console.log(`处理 ${total} 首，更新 ${changed} 首的题材标签`);
