# 诗词典藏 · 中华上下五千年诗词软件

一款离线安卓应用，收录自先秦至近代的中华历代诗词，多维分类浏览、诗人全集、名句集锦、今日荐读，界面为中国古典宣纸朱砂风格。

## 功能

- **今日荐读**：每天自动换一首（按日期随机），可手动"换一首"，可收藏
- **多维分类**：朝代 / 题材 / 体裁 / 诗人 / 教材必背，同一首诗可属多个类别，支持组合浏览
- **诗人长廊**：按朝代分组，含诗人生平小传与收录作品全集入口
- **诗词详情**：正文、白话译文、注释、**创作背景故事**、可点击的分类标签（部分全集语料条目暂无译文/背景，持续补全中）
- **名句集锦**：千古名句单独成页，点击跳转原词
- **搜索**：诗名 / 诗人 / 诗句 / 名句
- **收藏夹**：本地保存

## 数据构成（两层加载）

| 层 | 内容 | 说明 |
|----|------|------|
| `assets/poems/` | 精选名篇集 | 每首均含正文、译文、注释、创作背景、题材标签、教材标注；由数据代理逐首撰写，启动时加载 |
| `assets/bulk/` | 历代全集语料 | 来自公有领域开源语料库 chinese-poetry（《御定全唐詩》系、全宋词、全元曲、花间集、南唐、纳兰性德、诗经、楚辞、宋诗大家），繁体转简体，正文以底本为准（保留个别异文）；后台静默加载 |

收录规模与作者全集情况见 App 内"我的收藏"页下方统计或数据目录 `data/` 下脚本输出。

## 工程结构

```
shici/                     # Android 工程（Kotlin + Jetpack Compose）
  app/src/main/java/com/zhonghua/shici/
    MainActivity.kt        # 导航骨架 + 底部导航
    data/Repo.kt           # 数据仓库（两段式加载、分类索引、搜索、今日推荐、收藏）
    ui/theme/              # 宣纸/朱砂/黛青/藤黄 主题
    ui/screens/            # 首页/分类/诗人/详情/搜索/收藏/名句
data/
  SCHEMA.md                # 精选数据规范
  poems/                   # 精选集 JSON（代理生成）
  poems-bulk/              # 全集分块 JSON（语料导入）
  corpus/                  # 原始语料下载缓存
  validate.js              # 数据校验
  import-corpus.js         # 语料导入（繁转简/分类/去重）
  select-famous.js         # 名篇选题（背景补全流水线）
  fill-backgrounds.js      # 背景补全合并
tools/                     # JDK17 / Gradle 8.9 / Android SDK / 模拟器（本机自包含工具链）
```

## 构建

```bash
export JAVA_HOME=<tools/jdk/...> ANDROID_HOME=<tools/android-sdk>
cd shici
../tools/gradle/gradle-8.9/bin/gradle.bat assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk
```

## 继续收集 / 补全背景（可无限增补）

1. **增收语料**：往 `data/corpus/` 下载 chinese-poetry 其他集（如《御定全唐詩》全量、明诗、清诗），在 `import-corpus.js` 增加作者清单后运行 `node import-corpus.js`
2. **补背景**：`node select-famous.js roundNN.json 40` 选题 → 代理按 `fill-todo` 内说明生成 → `node fill-backgrounds.js` 合并
3. 同步 `data/poems` → `assets/poems`、`data/poems-bulk` → `assets/bulk`，重新打包
