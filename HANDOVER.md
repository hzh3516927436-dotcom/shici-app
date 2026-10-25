# 《诗词典藏》项目交接文档

> 面向接手的 AI 助手（WorkBuddy / Qoder / Claude / GPT 等）或人类开发者。
> 读完本文档即可独立完成：扩充诗词/文章/佳句、修复缺陷、重新打包发布。
> 文档更新日期：2026-10-25

---

## 一、项目是什么

一款**离线安卓应用**（Kotlin + Jetpack Compose），收录中华历代诗词、古文与现当代名句：

| 内容 | 数量（当前） | 来源 |
|------|------|------|
| 作品总量（诗词/文章/辞赋） | 67,585 件 | 双层结构，见下 |
| ├ 精选层（含译文/注释/创作背景/题材标签） | 406 件 | AI 逐首撰写（组号 G1~G13） |
| └ 全集层（正文为主） | 67,182 件 | 开源语料库 chinese-poetry 导入 |
| 佳句库 | 2,817 条（含赏析 2,142 条） | 手写（62）+ 语料自动（2080）+ AI 轮次（round01-26）+ 现代名句（M01-M08，675 条）+ 精选集自动关联 |
| 创作背景已填充 | 511+ 首 | round01-10、14、15、16 合并 |
| 诗人/作者小传 | 145+ 位 | 精选层自带 + authors.tang.json |
| 用户自添加（诗词/文章/好句） | 本地存储 | App 内"＋添加"，可搜索可收藏 |

功能：今日荐读（可换）、每日佳句赏析（可换/可收藏）、多维分类（朝代/题材/体裁/教材）、诗人长廊与小传、名句集锦、搜索（防抖 + 历史 + 好句维度）、收藏夹（诗词+佳句）、用户自添加。

---

## 二、目录结构与环境

```
<workspace>/                        # 项目根
├── 诗词典藏-最终版.apk             # 交付产物（22MB）
├── README.md                       # 面向用户/访客的说明
├── shici/                          # Android 工程 ★
│   ├── app/src/main/java/com/zhonghua/shici/
│   │   ├── MainActivity.kt         # 导航栈 + 底部导航（手写路由，非 Navigation 库）
│   │   ├── data/Repo.kt            # ★ 核心数据仓库（线程安全规则见第四节）
│   │   ├── data/UserStore.kt       # 用户自添加内容（本地 JSON 持久化）
│   │   ├── data/Models.kt          # Poem/Poet/NoteItem/FamousLine
│   │   └── ui/screens/*.kt         # Home/Browse/Poets/Search/UserAdd/QuoteDetail 等
│   ├── app/src/main/assets/
│   │   ├── poems/                  # 精选层 JSON（26 个文件）
│   │   ├── bulk/                   # 全集层 JSON（56 个分块，25MB）
│   │   └── famous.json             # 佳句库（构建脚本产物）
│   └── app/build/outputs/apk/debug/app-debug.apk
├── data/                           # 数据管线 ★
│   ├── SCHEMA.md                   # 精选层数据规范（写数据前必读）
│   ├── poems/                      # 精选层源文件（同步到 assets/poems）
│   ├── poems-bulk/                 # 全集层源文件（同步到 assets/bulk）
│   ├── famous-source.json          # 佳句源库（唯一可信源）
│   ├── famous-add/roundXX.json     # 佳句轮次产出（AI 写）
│   ├── famous-add/MXX.json         # 现代名句轮次产出（AI 写）
│   ├── fill-todo/roundXX.json      # 背景补全选题（脚本生成）
│   ├── fill-done/roundXX.json      # 背景补全成果（AI 写）
│   ├── fill-state.json             # 流水线状态（launched/phase）
│   ├── corpus/                     # 原始语料缓存（35MB，可再下载）
│   ├── validate.js                 # 精选层校验
│   ├── import-corpus.js            # 语料导入（繁转简/分类/去重/稳定ID）
│   ├── select-famous.js            # 背景补全选题
│   ├── fill-backgrounds.js         # 背景合并（幂等）
│   ├── merge-famous.js             # 佳句合并（幂等）
│   ├── build-famous.js             # 生成 assets/famous.json（含出处匹配）
│   ├── authors-in-corpus.json      # 库内作者清单（给采集代理用）
│   └── node_modules/               # opencc-js（繁转简）
├── tools/                          # 自包含工具链（~3GB，勿提交 git）
│   ├── jdk/jdk-17.0.20.1+1/        # Temurin JDK 17
│   ├── gradle/gradle-8.9/
│   ├── android-sdk/                # platform 35 / build-tools 35.0.0 / emulator / system-images
│   └── dl/                         # 安装包缓存
└── shots/                          # 测试截图
```

**环境变量**（所有构建/调试命令都需要）：
```bash
export JAVA_HOME="<workspace>/tools/jdk/jdk-17.0.20.1+1"
export ANDROID_HOME="<workspace>/tools/android-sdk"
```

**AVD**：`shici_avd`（Pixel 6 · API 35 · x86_64）。启动模拟器（务必重定向输出，见 6.3 坑）：
```bash
<workspace>/tools/android-sdk/emulator/emulator.exe -avd shici_avd \
  -no-window -gpu swiftshader_indirect -no-snapshot -no-audio -no-boot-anim \
  > <workspace>/tools/emu.log 2>&1 &
```

---

## 三、构建 / 安装 / 调试

```bash
cd <workspace>/shici
"$JAVA_HOME/bin/java" -version   # 确认 17
<workspace>/tools/gradle/gradle-8.9/bin/gradle.bat --no-daemon assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk
```

常用 adb（`<workspace>/tools/android-sdk/platform-tools/adb.exe`）：
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk   # 覆盖安装（数据保留）
adb shell am start -n com.zhonghua.shici/.MainActivity      # 启动
adb shell am force-stop com.zhonghua.shici                  # 强停
adb shell pm clear com.zhonghua.shici                       # 清空全部用户数据（出厂态）
adb logcat -d | grep -A 12 "FATAL EXCEPTION"                # 崩溃栈
adb exec-out screencap -p > shot.png                        # 截图
```

UI 自动化注意：`adb shell input text` **无法输入中文**；截图是设备像素的缩放图，点击坐标要用 uiautomator dump 的 bounds，别用截图坐标（差 1.2 倍）。

---

## 四、血泪教训（改代码前必读，违反即崩溃/卡死）

1. **线程安全**：数据加载在后台线程（6.7 万件需数秒），主线程随时读。共享集合必须线程安全：
   - `Repo.poems` = `mutableStateListOf`（快照列表）✓
   - `Repo.poets` / `Repo.byId` = **ConcurrentHashMap**（曾被改成 HashMap 导致随机闪退）
   - `Repo.appreciations` = `mutableStateListOf`（曾被改成普通 List 导致随机闪退）
2. **禁止主线程扫全库**：任何 O(作品数) 的统计必须走 `rebuildIndexes()` 预建索引（`authorPoemsMap`/朝代/题材/体裁缓存），改完数据记得 `bumpVersions()`。
3. **大列表必须 LazyColumn**：诗人详情曾用 `Column+verticalScroll+forEach` 一次组装白居易 2975 张卡片 → ANR 闪退。已改 LazyColumn。新增长列表同样遵守。
4. **批量写入**：向 `Repo.poems` 装载大量数据时，先在普通 List 排好序，最后一次 `poems.clear(); poems.addAll(...)`，禁止逐条 add 6.7 万次。
5. **largeHeap**：Manifest 已开，勿删。
6. **代理并发上限 1~2**：本平台同时最多 2 个子代理，超发会秒报 `concurrency limit exceeded`（无损，重试即可）；代理运行期不要调用重量级模型工具（会挤占配额）。
7. **自动化管道输出重定向**：模拟器/长任务必须 `> log 2>&1`，管道 `| head` 会 SIGPIPE 杀掉进程。
8. **AI 直接生成的代码要复查**：历史上有过非法语法、漏 import、泛型写错等，先 `assembleDebug` 再交付。

---

## 五、数据规范与三种扩充方式

### 5.0 ID 约定（不可变，勿重排）
- 精选层：`G组号-序号`（如 G4-007）
- 全集层：`B + sha1(作者|标题|正文前16字) 前10位`（内容稳定哈希，重跑导入不变）
- 佳句：`F%03d`（famous.json 内）/ `P-<作品id>`（作品名句自动关联）
- 用户添加：`U<时间戳>` / `UF<时间戳>`

### 5.1 方式 A：AI 撰写精选层（含译文/注释/背景，质量最高）
1. 读 `data/SCHEMA.md`（字段、枚举值、质量红线）
2. 按规范写 JSON 到 `data/poems/poems_<主题>.json`（poems + poets 两数组）
3. 校验：`cd data && node validate.js`（必须 0 错误）
4. 同步：`cp poems/*.json ../shici/app/src/main/assets/poems/`
5. 重打包

**给 AI 的参考提示词**（实测有效）：
> 你是古典诗词数据生成代理。用 Read 工具读取 …\data\SCHEMA.md，严格遵守全部规范。组号 G14（id 前缀 G14-，序号从 001 连续递增）。生成以下 N 首诗到 …\data\poems\poems_xxx.json……（列清单：标题/作者/体裁）……每首含 body/translation/notes(2-6条)/background(150-300字，史实可靠拿不准不写)/themes/themes 自行判断。正文没把握就换更有把握的名篇，保持总数。完成后回复数量与把握问题，不贴 JSON。

### 5.2 方式 B：为全集层补创作背景（不写正文，量大）
1. `cd data && node select-famous.js roundNN.json 40` → 生成 `fill-todo/roundNN.json`（自动避开已补）
2. AI 读该文件，为每首写 background(150~250字) + 可选 famous 数组，写到 `fill-done/roundNN.json`，格式：
   `{"fills":[{"id":"与todo完全一致","background":"…","famous":["正文原句"]},...]}`
3. 合并：`node fill-backgrounds.js fill-done/roundNN.json`（幂等，只填空白）
4. 同步 + 重打包

### 5.3 方式 C：佳句库扩充（含现代名句）
1. 读 `data/famous-source.json`（严禁与既有 line 重复）
2. AI 写 80 条到 `data/famous-add/roundXX.json`（古典）或 `MXX.json`（现代），格式：
   `{"lines":[{"a":"作者","t":"篇名/书名前缀","line":"名句原文1~2句","ap":"赏析30~60字","dynasty":"当代"}]}`
   （现代条目必须带 `"dynasty":"当代"`；只收有把握广泛流传的句子，措辞无把握宁可不收——**网络伪托句极多**，M 系列每条都应网络核实）
3. 合并：`node merge-famous.js && node build-famous.js`（幂等，自动按 line 去重 + 匹配原篇跳转）
4. 同步 + 重打包

### 5.4 方式 D：导入开源语料（chinese-poetry 仓库）
1. 下载对应目录 JSON 到 `data/corpus/`（jsDelivr：`https://cdn.jsdelivr.net/gh/chinese-poetry/chinese-poetry@master/<URL编码目录>/<文件>`；偶发 404 时重试或换 fastly.jsdelivr.net / gitee 镜像）
2. 在 `import-corpus.js` 顶部作者清单加入目标作者（或新增处理器函数，照 nantang/shijing 的样子写）
3. `node import-corpus.js`（幂等；繁体自动转简；**会重放 fill-done 的背景补全**，所以背景不丢）
4. 同步 `poems-bulk/bulk_*.json` → assets/bulk，重打包
5. 已接入的语料源：全唐诗(58文件)/全宋诗(255)/宋词(22)/元曲/花间集/南唐/纳兰性德/诗经/楚辞/曹操/古文观止

### 5.5 同步与打包（每次改数据后）
```bash
cd data
cp poems/*.json ../shici/app/src/main/assets/poems/
cp poems-bulk/bulk_*.json ../shici/app/src/main/assets/bulk/
# famous.json 由 build-famous.js 直接写进 assets，无需手拷
cd ../shici && gradle assembleDebug
```

---

## 六、已知问题与待办（按优先级）

1. 【中】分类页不含"我的"分类：用户自添加作品 dynasty="我的" 不在 `dynastyOrder`，只能从题材"我的"进入。可在 BrowseScreen 加"我的"标签。
2. 【中】名句集锦未排序混排：顺序为 hand-written → rounds → M 系列 → 自动关联 → 用户添加。可按需重排。
3. 【低】收藏页删除用户作品后，收藏 set 里的孤儿 id 不清理（无害，仅冗余）。
4. 【低】`dailyPoem` 的 prefs key 按天累积不清理（无害）。
5. 【低】搜索结果排序可再优化：当前"标题/作者匹配 > 正文匹配"，可加"作者完全相等"置顶。
6. 【信息】用户真机反馈过边缘裁切：已加 `windowLayoutInDisplayCutoutMode=never`(values-v27)；若复现请要未裁剪的完整截图按机型处理。
7. 【信息】背景补全在 round17/18 被中断未跑完（round17 的 40 首选题在 fill-todo 已清理回池），随时可按 5.2 续跑。

---

## 七、发布到 GitHub（两种方式）

### 7.0 先做：创建 `.gitignore`（项目根目录，内容如下）
```gitignore
tools/
data/corpus/
data/node_modules/
shici/.gradle/
shici/build/
shici/app/build/
shici/local.properties
*.apk
shots/
data/fill-state.json
```
> 说明：tools/ 约 3GB 且可按第三节重装；corpus/ 35MB 可重新下载；这两项务必排除。其余 data/ 内容（精选集、佳句库、脚本）**应当提交**，它们是项目的核心资产。

### 7.1 方式一：网页上传（最简单，适合不会 git）
1. 注册/登录 github.com → 右上角 "+" → New repository
2. 填仓库名（如 `shici-app`）、选 Public、勾选 "Add a README"（或不要，用项目里的）
3. 创建后点 "uploading an existing file"（或 Add file → Upload files）
4. **注意**：网页单次上传限 100 个文件，建议用 7-Zip 把 `shici/`、`data/poems/`、`data/poems-bulk/`、`data/*.js`、`data/*.json`、`data/SCHEMA.md`、`README.md` 分别打成 zip 上传再说明结构——更好的方式是学一下 git（方式二）

### 7.2 方式二：git 命令行（推荐，一次配好终身用）
```bash
cd <workspace>
git init
# 创建 .gitignore（内容见 7.0）
git add .
git commit -m "诗词典藏：安卓离线诗词应用 v1.0（6.7万件作品 + 2817条佳句）"
# 在 github.com 网页上新建空仓库后（不要勾选自动 README）：
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git branch -M main
git push -u origin main
# 推送时若很大（data/poems-bulk 约 25MB、json 若干），首次可能需几分钟
```
后续更新：
```bash
git add . && git commit -m "说明改了什么" && git push
```

### 7.3 发布 Release（附 APK 供人下载）
1. 仓库页 → Releases → Draft a new release
2. Tag 填 `v1.0`，标题随意，描述功能清单
3. 把 `诗词典藏-最终版.apk` 拖进附件区 → Publish

### 7.4 版权与许可说明（建议写进仓库 README）
- 古代诗文均为公有领域古籍（《御定全唐詩》《全宋词》等，经 chinese-poetry 开源项目整理）
- 现代名句（剑来/云边有个小卖部等）仅为**1~2 句简短引用**并注明出处，配原创赏析，属合理引用范围；已人工剔除网络伪托句
- 代码许可建议 MIT；chinese-poetry 数据遵循其原仓库许可（MIT）
- 开源后若收到权利人异议，承诺移除相应条目即可

### 7.5 可选：GitHub Actions 自动打包（进阶）
在仓库 `.github/workflows/build.yml` 加一个 workflow，用 `actions/setup-java@v4`（Temurin 17）+ `android-actions/setup-android` + `./gradlew assembleDebug`，每次 push 自动产出 APK 到 Artifacts。项目用 gradle-8.9 + AGP 8.6.1 + Kotlin 2.0.21，workflow 里保持同版本即可。首次需要生成 `gradle wrapper`（`gradle wrapper --gradle-version 8.9`）并提交 gradle/wrapper 文件。

---

## 八、给接手 AI 的三句话

1. **改数据走管线**（5.2/5.3 的脚本都是幂等的），别手改 bulk 分块文件——重跑导入会覆盖手改。
2. **改代码先读第四节**，两次重大事故（随机闪退、诗人页 ANR）都是违反那里的规则造成的。
3. **每次改动后**：`validate.js` → 同步 assets → `assembleDebug` → 装模拟器实测 → 再交付。
