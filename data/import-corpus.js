// 从 chinese-poetry 语料导入作者全集（精简模式：无译文/背景，正文繁转简）
// 用法: node import-corpus.js            # 处理 data/corpus/ 下已下载文件
//       node import-corpus.js --stat     # 只统计各作者存量
const fs = require('fs');
const path = require('path');
const OpenCC = require('opencc-js');
const t2s = OpenCC.Converter({ from: 't', to: 'cn' });

const WS = __dirname;
const CORPUS = path.join(WS, 'corpus');
const OUT = path.join(WS, 'poems-bulk');

// ---------- 目标作者（全集导入） ----------
const TANG_AUTHORS = ['李白','杜甫','白居易','王维','李商隐','杜牧','孟浩然','王昌龄','刘禹锡','柳宗元','韦应物','岑参','高适','贾岛','孟郊','张籍','王建','元稹','韩愈','李贺','温庭筠','罗隐','许浑','张祜','钱起','刘长卿','卢纶','李益','戴叔伦','杜荀鹤','崔颢','崔涂','韦庄','郑谷','曹松','崔道融'];
const SONG_CI_AUTHORS = ['苏轼','辛弃疾','李清照','欧阳修','柳永','晏殊','晏几道','秦观','周邦彦','姜夔','贺铸','张先','吴文英','王沂孙','张孝祥','陆游','朱敦儒','陈亮','刘过','叶梦得','晁补之','张元干','赵长卿','毛滂'];
const YUAN_AUTHORS = ['马致远','张可久','关汉卿','白朴','张养浩','乔吉','王实甫','卢挚','徐再思','郑光祖','刘时中','贯云石','睢景臣','薛昂夫','刘致'];

// 宋诗全集作者（按用户标准，长尾小家质量不及陆游/杨万里存诗，故两位也收全集）
const SONG_SHI_AUTHORS = ['苏轼','王安石','黄庭坚','范成大','欧阳修','梅尧臣','陈师道','陈与义','张耒','苏辙','曾巩','曾几','林逋','寇准','司马光','王禹偁','苏舜钦','文同','朱熹','文天祥','范仲淹','戴复古','陆游','杨万里'];
// 陆游名篇题录（前缀匹配）
const LU_YOU_TITLES = ['游山西村','临安春雨初霁','书愤','示儿','秋夜将晓出篱门迎凉有感','十一月四日风雨大作','冬夜读书示子聿','剑门道中遇微雨','病起书怀','梅花绝句','读书','幽居初夏','秋思','观村童戏溪上','雪夜感旧','大风','浮世','灯下读书','读书有感','秋夜读书','夏夜','夜雨','秋雨','书室','幽居','秋兴','春日','初夏绝句','梅花','落花','秋月','夜坐','独坐','春雨','夏夜泛舟','六月十四日宿东林寺','舟中作','秋郊','晚窗','小园','独立','思故山','故山','文章','白露','寒夜','冬夜','夜归','马上作','出行','马上','春残','初夏','立春','春晴','晚春','初秋','秋晚','晚秋','冬暖','岁暮','新春'];
// 杨万里名篇题录（前缀匹配）
const YWL_TITLES = ['小池','晓出净慈寺送林子方','宿新市徐公店','稚子弄冰','舟过安仁','闲居初夏午睡起','过松源晨炊漆公店','桑茶坑道中','春日','南溪早春','插秧歌','雪后晚晴','夏夜追凉','夜宿东渚','小雨','雨后田间杂记','初秋行圃','秋凉晚步','宿池州','过百家渡','入常山界','过石塘','万安道中书事','发通衢驿','过松源','二月一日晓渡','舟过安仁','雪晓','观雪','梅花','腊里立春','立春日','春晴','晚风','暮热游荷池上','暮行','晚归','夜坐','晓行','早行'];

const norm = s => t2s(s || '').replace(/[\s，。、·！？；：""''《》（）()\-—]/g, '');
const firstKey = (author, body) => author + '|' + norm(body).slice(0, 12);
const titleKey = (author, title) => author + '|' + norm(title).slice(0, 20);

// ---------- 题材启发式（传统诗学类目） ----------
function guessThemes(title, body) {
    const t = title, b = body.slice(0, 120);
    const th = [];
    const has = (re) => re.test(t) || re.test(b);
    if (has(/送|别|赠|饯|寄.*友|留别|怀人|忆.*友|离亭|怀远|怀友/)) th.push('送别怀人');
    if (has(/塞上|塞下|从军|出塞|征人|戍|军中|战|烽|鼓角|弓刀|铁衣|瀚海|轮台|楼兰|征戍/)) th.push('边塞征戍');
    if (has(/思归|故乡|乡心|客中|羁旅|他乡|归梦|望乡|客舍|天涯|旅夜|旅次|乡关/)) th.push('羁旅思乡');
    if (has(/田园|村居|田家|农家|耕|牧童|樵|渔父|钓叟|村晚|乡村|柴门|桑麻/)) th.push('山水田园');
    if (has(/怀古|咏史|古迹|故垒|六朝|兴亡|台城|乌衣|铜雀|荒台/)) th.push('咏史怀古');
    if (has(/闺怨|宫怨|美人|罗裳|妆楼|佳期|同心|欢情|相思|深闺|长信|长门|宫词|玉闺/)) th.push('闺怨宫怨');
    if (has(/元日|清明|七夕|中秋|重阳|除夜|寒食|上元|九日|端午|元宵|守岁/)) th.push('节令风俗');
    if (has(/哭|悼|挽|哀|坟|墓|宿草|伤逝|悼亡/)) th.push('悼亡伤逝');
    if (has(/民|租|税|役|贫|饥|卖|赋敛|朱门|征敛|虐|苦役/)) th.push('讽喻刺世');
    if (has(/报国|山河|中原|胡尘|王师|九州|金戈|铁马|收复|神州|家祭|兴复|国破|忧时/)) th.push('爱国忧时');
    if (has(/题画|画中|题壁|书壁|壁间|画鹰|画竹|画梅|看画/)) th.push('题画题壁');
    if (has(/闲居|幽居|隐|逸|渔樵|山居|闲适|闲行|闲望|静坐|独坐|焚香|抚琴|弹琴|柴扉|掩扉/)) th.push('闲适隐逸');
    if (has(/游仙|梦游|仙山|蓬莱|瑶台|魂梦|一梦|梦中|梦觉/)) th.push('游仙梦幻');
    if (has(/干谒|上书|投赠|酬|奉和|见赠|次韵|寄呈|简寄|呈./)) th.push('干谒酬赠');
    if (has(/儿童|稚子|小儿|幼|垂钓(?!.*叟)/)) th.push('童趣生活');
    if (/^咏|^题.*[梅兰竹菊柳蝉鹤雁燕莺蜂松荷莲牡丹杏桃梨]|梅|兰|竹|菊|柳|蝉|鹤|雁|燕|莺|蜂|松|荷|莲|牡丹|杏|桃|梨/.test(t)) th.push('咏物言志');
    if (has(/山|水|溪|湖|江|云|月|雪|雨|风|花|草|春|秋|晓|晚|晴|烟/)) th.push('写景抒情');
    if (has(/浮生|人生|世事|今古|禅|空|悟|理|玄|道|无为/)) th.push('哲理玄言');
    return [...new Set(th)].slice(0, 3);
}

// ---------- 体裁启发式（近体诗按字数与句数判断） ----------
function guessForm(title, body, isCi) {
    if (isCi) return { form: '词', tune: null };
    if (/词|令$|慢$|引$|近$|调歌/.test(title) && !/诗/.test(title)) return { form: '词', tune: title.split(/[·]/)[0] };
    const flat = body.replace(/\s/g, '');
    // 按所有标点切出分句，检验是否齐言五字/七字
    const clauses = flat.split(/[，。？！；、]/).filter(s => s.length > 0);
    if (clauses.length === 0) return { form: '五言古诗', tune: null };
    const lens = new Set(clauses.map(c => c.length));
    const per = [...lens][0];
    if (lens.size === 1 && (per === 5 || per === 7)) {
        const sents = flat.split(/[。？！；]/).filter(s => s.length > 0).length;
        const yan = per === 5 ? '五言' : '七言';
        if (sents === 4) return { form: yan + '绝句', tune: null };
        if (sents === 8) return { form: yan + '律诗', tune: null };
        return { form: yan + '古诗', tune: null };
    }
    if (/乐府|歌行|行$|吟$|引$|谣$|曲$/.test(title)) return { form: '乐府诗', tune: null };
    return { form: per === 7 ? '七言古诗' : '五言古诗', tune: null };
}

// ---------- 读取现有数据，建立去重键 ----------
function loadExistingKeys() {
    const keys = new Set();
    const dir = path.join(WS, 'poems');
    if (!fs.existsSync(dir)) return keys;
    for (const f of fs.readdirSync(dir).filter(f => f.endsWith('.json'))) {
        try {
            const root = JSON.parse(fs.readFileSync(path.join(dir, f), 'utf8'));
            for (const p of (root.poems || [])) {
                keys.add(firstKey(p.author, p.body));
            }
        } catch (e) { }
    }
    return keys;
}

// ---------- 主流程 ----------
const existing = loadExistingKeys();
const seen = new Set(existing);
const bulkPoems = [];
const authorDesc = {}; // 作者小传（来自 authors.*.json）

function readAuthorsDesc(file) {
    const fp = path.join(CORPUS, file);
    if (!fs.existsSync(fp)) return;
    try {
        const j = JSON.parse(fs.readFileSync(fp, 'utf8'));
        for (const [name, info] of Object.entries(j)) {
            authorDesc[t2s(name)] = info.desc ? t2s(info.desc) : '';
        }
    } catch (e) { }
}

function processFile(fp, dynasty, authors, isCi) {
    let arr;
    try { arr = JSON.parse(fs.readFileSync(fp, 'utf8')); } catch (e) { console.log('  解析失败', path.basename(fp)); return; }
    let n = 0;
    for (const p of arr) {
        const author = t2s(p.author || '');
        if (!authors.includes(author)) continue;
        let title, body, tune = null;
        if (isCi) {
            body = (p.paragraphs || []).map(t2s).join('\n');
            if (p.rhythmic) tune = t2s(p.rhythmic);
            const snippet = body.split('\n')[0].replace(/[，。、！？；：\s]/g, '').slice(0, 7);
            title = tune ? (snippet ? `${tune}·${snippet}` : tune) : (snippet || '无名词');
        } else {
            title = t2s(p.title || '').trim();
            body = (p.paragraphs || []).map(t2s).join('\n');
        }
        if (!title || !body || body.length < 4) continue;
        const key = firstKey(author, body);
        if (seen.has(key)) continue;
        seen.add(key);
        const form = isCi ? '词' : guessForm(title, body, false).form;
        if (isCi && !tune) tune = title.split(/[·]/)[0] || '';
        bulkPoems.push({
            id: null, // 稍后统一编号
            title: title.replace(/[（(]Adds?$/, ''),
            author, dynasty, form,
            ...(tune ? { tune } : {}),
            themes: guessThemes(title, body),
            body
        });        n++;
    }
    if (n) console.log(`  ${path.basename(fp)}: +${n}`);
}

function main() {
    if (!fs.existsSync(CORPUS)) { console.log('corpus 目录不存在'); return; }
    readAuthorsDesc('authors.tang.json');
    readAuthorsDesc('authors.song.json');

    const files = fs.readdirSync(CORPUS).filter(f => f.endsWith('.json')).sort();
    const readJson = (fp) => { try { return JSON.parse(fs.readFileSync(fp, 'utf8')); } catch (e) { return []; } };
    let seq = 0;
    let f;
    for (const f of files) {
        if (f.startsWith('poet.tang.')) processFile(path.join(CORPUS, f), '唐', TANG_AUTHORS, false);
        else if (f.startsWith('poet.song.')) processSongShi(path.join(CORPUS, f));
        else if (f.startsWith('ci.song.')) processFile(path.join(CORPUS, f), '宋', SONG_CI_AUTHORS, true);
    }

    // ---------- 宋诗（大家全集 + 陆游/杨万里名篇精选） ----------
    function processSongShi(fp) {
        let arr;
        try { arr = JSON.parse(fs.readFileSync(fp, 'utf8')); } catch (e) { return; }
        let n = 0;
        for (const p of arr) {
            const author = t2s(p.author || '');
            if (!SONG_SHI_AUTHORS.includes(author)) continue;
            const title = t2s(p.title || '').trim();
            const body = (p.paragraphs || []).map(t2s).join('\n');
            addEntry(title, author, '宋', guessForm(title, body, false).form, null, body);
            n++;
        }
        if (n) console.log(`  ${path.basename(fp)}: +${n} (songshi)`);
    }

    // ---------- 通用条目 ----------
    function addEntry(title, author, dynasty, form, tune, body) {
        title = (title || '').trim(); body = (body || '').trim();
        if (!title || !body || body.length < 4) return;
        const key = firstKey(author, body);
        if (seen.has(key)) return;
        seen.add(key);
        bulkPoems.push({
            id: null,
            title, author, dynasty, form,
            ...(tune ? { tune } : {}),
            themes: guessThemes(title, body),
            body
        });
    }

    // ---------- 南唐（李璟/李煜/冯延巳 全集） ----------
    if (fs.existsSync(f = path.join(CORPUS, 'nantang-poetrys.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            addEntry(t2s(p.title || (p.rhythmic ? t2s(p.rhythmic) : '无题')), t2s(p.author), '五代', '词', p.rhythmic ? t2s(p.rhythmic) : null, (p.paragraphs || []).map(t2s).join('\n'));
            n++;
        }
        if (n) console.log('  nantang: +' + n);
    }

    // ---------- 花间集（全部） ----------
    for (const g of files.filter(f => f.startsWith('huajianji-'))) {
        let n = 0;
        for (const p of readJson(path.join(CORPUS, g))) {
            addEntry(t2s(p.title || p.rhythmic), t2s(p.author), '五代', '词', p.rhythmic ? t2s(p.rhythmic) : null, (p.paragraphs || []).map(t2s).join('\n'));
            n++;
        }
        if (n) console.log(`  ${g}: +${n}`);
    }

    // ---------- 纳兰性德全集 ----------
    if (fs.existsSync(f = path.join(CORPUS, 'nalan.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            const title = t2s(p.title || ''), body = (p.para || p.paragraphs || []).map(t2s).join('\n');
            const isCi = p.para || title.includes('·') || /令|慢|引|近|歌头|奴娇|溪沙|陂塘/.test(title);
            const tune = isCi && title.includes('·') ? title.split('·')[0] : null;
            addEntry(title, '纳兰性德', '清', isCi ? '词' : guessForm(title, body, false).form, isCi ? (tune || title.split('·')[0]) : null, body);
            n++;
        }
        if (n) console.log('  nalan: +' + n);
    }

    // ---------- 元曲名家全集 ----------
    if (fs.existsSync(f = path.join(CORPUS, 'yuanqu.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            const author = t2s(p.author || '');
            if (!YUAN_AUTHORS.includes(author)) continue;
            const title = t2s(p.title || '');
            const tune = title.includes('・') ? title.split('・').pop().split('/').pop() : (title.split('/')[1] || title);
            addEntry(title, author, '元', '散曲', tune || null, (p.paragraphs || []).map(t2s).join('\n'));
            n++;
        }
        if (n) console.log('  yuanqu: +' + n);
    }

    // ---------- 诗经全集 ----------
    if (fs.existsSync(f = path.join(CORPUS, 'shijing.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            addEntry(t2s(p.title) + '·' + t2s(p.section || ''), '无名氏', '先秦', '四言诗', null, (p.content || []).map(t2s).join('\n'));
            n++;
        }
        if (n) console.log('  shijing: +' + n);
    }

    // ---------- 曹操诗集全集 ----------
    if (fs.existsSync(f = path.join(CORPUS, 'caocao.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            const body = (p.paragraphs || []).map(t2s).join('\n');
            addEntry(t2s(p.title || ''), t2s(p.author || '曹操'), '汉', '乐府诗', null, body);
            n++;
        }
        if (n) console.log('  caocao: +' + n);
    }

    // ---------- 古文观止（传世文章全集：散文/辞赋/骈文） ----------
    if (fs.existsSync(f = path.join(CORPUS, 'guwenguanzhi.json'))) {
        let n = 0;
        const book = readJson(f);
        const PIANWEN = ['滕王阁序', '与朱元思书', '答谢中书书', '与陈伯之书', '为徐敬业讨武曌檄'];
        const DMAP = { '先秦': '先秦', '秦': '先秦', '周': '先秦', '战国': '先秦', '汉': '汉', '两汉': '汉', '後汉': '汉', '东汉': '汉', '西汉': '汉', '后汉': '汉', '三国': '三国', '魏': '魏晋', '魏晋': '魏晋', '晋': '魏晋', '东晋': '魏晋', '西晋': '魏晋', '南朝': '南北朝', '北朝': '南北朝', '北魏': '南北朝', '六朝': '南北朝', '隋': '隋', '唐': '唐', '唐代': '唐', '五代': '五代', '宋': '宋', '北宋': '宋', '南宋': '宋', '元': '元', '元代': '元', '明': '明', '明代': '明', '清': '清', '清代': '清' };
        // 六朝唐文卷作者按人定朝代（卷名无法区分）
        const A_DYN = { '王羲之': '魏晋', '陶渊明': '魏晋', '李密': '魏晋', '诸葛亮': '三国', '孔稚珪': '南北朝', '陶弘景': '南北朝', '吴均': '南北朝', '庾信': '南北朝', '江淹': '南北朝', '刘峻': '南北朝', '刘义庆': '南北朝', '王勃': '唐', '骆宾王': '唐', '李白': '唐', '李华': '唐', '魏征': '唐', '魏徵': '唐', '刘禹锡': '唐', '杜牧': '唐', '韩愈': '唐', '柳宗元': '唐' };
        for (const vol of (book.content || [])) {
            for (const art of (vol.content || [])) {
                const title = t2s(art.chapter || '').trim();
                const body = (art.paragraphs || []).map(t2s).join('\n');
                if (!title || !body) continue;
                const rawAuthor = t2s(art.author || '').trim();
                const author = ((rawAuthor.split(/[：:]/).pop() || '').split('·').pop() || '').trim() || '无名氏';
                let dynasty = A_DYN[author];
                if (!dynasty) {
                    const prefix = (rawAuthor.split(/[：:]/)[0] || '').trim();
                    if (DMAP[prefix]) dynasty = DMAP[prefix];
                    else {
                        const vt = vol.title || '';
                        dynasty = /周文|秦文|战国/.test(vt) ? '先秦' : /汉/.test(vt) ? '汉' : /六朝/.test(vt) ? '南北朝' : /唐/.test(vt) ? '唐' : /明/.test(vt) ? '明' : '宋';
                    }
                }
                let form = '散文';
                if (/赋$/.test(title) || title === '归去来兮辞') form = '辞赋';
                else if (PIANWEN.some(w => title.includes(w))) form = '骈文';
                addEntry(title, author, dynasty, form, null, body);
                n++;
            }
        }
        if (n) console.log('  guwenguanzhi: +' + n);
    }

    // ---------- 楚辞全集 ----------
    if (fs.existsSync(f = path.join(CORPUS, 'chuci.json'))) {
        let n = 0;
        for (const p of readJson(f)) {
            addEntry(t2s(p.title), t2s(p.author || '屈原'), '先秦', '楚辞体', null, (p.content || []).map(t2s).join('\n'));
            n++;
        }
        if (n) console.log('  chuci: +' + n);
    }

    // 按作者+朝代排序；id 用内容哈希保证跨批次稳定
    const crypto = require('crypto');
    bulkPoems.sort((a, b) => a.author.localeCompare(b.author, 'zh') || a.title.localeCompare(b.title, 'zh'));
    const idSet = new Set();
    for (const p of bulkPoems) {
        const flat = (p.body || '').replace(/[\s，。、·！？；：""''《》（）()\-—]/g, '');
        let base = 'B' + crypto.createHash('sha1').update(p.author + '|' + p.title + '|' + flat.slice(0, 16)).digest('hex').slice(0, 10);
        let id = base;
        let n = 1;
        while (idSet.has(id)) { id = base + '-' + n++; }
        idSet.add(id);
        p.id = id;
    }

    // ---------- 重放已完成的背景补全（跨批次保留） ----------
    const fillsDir = path.join(__dirname, 'fill-done');
    if (fs.existsSync(fillsDir)) {
        let applied = 0;
        for (const fd of fs.readdirSync(fillsDir).filter(x => x.endsWith('.json'))) {
            try {
                const j = JSON.parse(fs.readFileSync(path.join(fillsDir, fd), 'utf8'));
                for (const it of (j.fills || [])) {
                    const hit = bulkPoems.find(p => p.id === it.id);
                    if (hit && !hit.background && it.background) {
                        hit.background = it.background;
                        if (it.translation && !hit.translation) hit.translation = it.translation;
                        applied++;
                    }
                }
            } catch (e) { }
        }
        if (applied) console.log(`  已重放背景补全: ${applied} 首`);
    }

    // 输出分块（每 1200 首一个文件）
    fs.mkdirSync(OUT, { recursive: true });
    const CHUNK = 1200;
    let fi = 0;
    for (let i = 0; i < bulkPoems.length; i += CHUNK, fi++) {
        const chunk = bulkPoems.slice(i, i + CHUNK);
        const dyn = chunk[0].dynasty;
        fs.writeFileSync(path.join(OUT, `bulk_${String(fi).padStart(2, '0')}.json`),
            JSON.stringify({ poems: chunk }, null, 0));
    }

    // 诗人小传存根（有 desc 用 desc）
    const dynByAuthor = {};
    bulkPoems.forEach(p => { dynByAuthor[p.author] = p.dynasty; });
    const poets = Object.keys(dynByAuthor).sort().map(name => ({
        name, dynasty: dynByAuthor[name], years: '生卒年不详', honor: '',
        bio: authorDesc[name] || ''
    }));
    fs.writeFileSync(path.join(OUT, 'poets_bulk.json'), JSON.stringify({ poets }, null, 0));

    const byAuthor = {};
    bulkPoems.forEach(p => { byAuthor[p.author] = (byAuthor[p.author] || 0) + 1; });
    console.log('\n===== 导入统计 =====');
    console.log(`总计 ${bulkPoems.length} 首，作者 ${Object.keys(byAuthor).length} 位，输出 ${fi} 个分块文件`);
    Object.entries(byAuthor).sort((a, b) => b[1] - a[1]).forEach(([a, c]) => console.log(`  ${a}: ${c}`));
}

main();
