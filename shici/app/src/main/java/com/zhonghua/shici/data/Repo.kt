package com.zhonghua.shici.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlin.random.Random

data class FamousLine(
    val id: String,
    val line: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val poemId: String,
    val appreciation: String
)

object Repo {

    val dynastyOrder = listOf("先秦", "汉", "三国", "魏晋", "南北朝", "隋", "唐", "五代", "宋", "元", "明", "清", "近代")
    val formOrder = listOf("四言诗", "楚辞体", "乐府诗", "五言古诗", "七言古诗", "五言绝句", "七言绝句", "五言律诗", "七言律诗", "词", "散曲", "辞赋", "骈文", "散文")
    val textbookValues = listOf("小学必背", "初中必背", "高中必背")

    val poems = mutableStateListOf<Poem>()

    /** 线程安全容器：加载在后台线程进行，主线程随时读取（普通 HashMap/List 会并发修改崩溃） */
    val poets = java.util.concurrent.ConcurrentHashMap<String, Poet>()
    val byId = java.util.concurrent.ConcurrentHashMap<String, Poem>()

    /** 每日佳句库：手写赏析条目 + 精选集名句自动生成条目 + 用户自添加 */
    val appreciations = androidx.compose.runtime.mutableStateListOf<FamousLine>()

    var poemVersion by mutableStateOf(0)
        private set
    var poetVersion by mutableStateOf(0)
        private set

    @Volatile
    var ready = false
        private set

    // ---------- 性能索引（O(n) 预建，O(1) 查询；主线程禁扫全库） ----------

    @Volatile
    private var authorPoemsMap: Map<String, List<Poem>> = emptyMap()
    private var statsVersion = -1
    private var dynastyCache: List<Pair<String, Int>> = emptyList()
    private var themesCache: List<Pair<String, Int>> = emptyList()
    private var formsCache: List<Pair<String, Int>> = emptyList()

    private fun rebuildIndexes() {
        val byAuthor = HashMap<String, MutableList<Poem>>()
        for (p in poems) byAuthor.getOrPut(p.author) { ArrayList() }.add(p)
        authorPoemsMap = byAuthor
        // 朝代/题材/体裁统计：单遍扫描
        val dyn = HashMap<String, Int>()
        val th = HashMap<String, Int>()
        val fm = HashMap<String, Int>()
        for (p in poems) {
            dyn[p.dynasty] = (dyn[p.dynasty] ?: 0) + 1
            fm[p.form] = (fm[p.form] ?: 0) + 1
            for (t in p.themes) th[t] = (th[t] ?: 0) + 1
        }
        dynastyCache = dynastyOrder.mapNotNull { dn -> dyn[dn]?.let { dn to it } }
        themesCache = th.entries.map { it.key to it.value }.sortedByDescending { it.second }
        formsCache = formOrder.mapNotNull { f -> fm[f]?.let { f to it } }
        statsVersion = poemVersion
    }

    private fun ensureStats() {
        if (statsVersion != poemVersion) rebuildIndexes()
    }

    private fun bumpVersions() {
        poemVersion++
        poetVersion++
        rebuildIndexes()
    }

    /** 供 UserStore 在并入用户内容后刷新界面 */
    fun bumpUserVersion() = bumpVersions()

    /** 删除用户作品 */
    fun removeById(id: String) {
        byId.remove(id)
        val idx = poems.indexOfFirst { it.id == id }
        if (idx >= 0) poems.removeAt(idx)
        bumpVersions()
    }

    fun removeAppreciation(id: String) {
        val idx = appreciations.indexOfFirst { it.id == id }
        if (idx >= 0) appreciations.removeAt(idx)
    }

    fun poemsOf(author: String): List<Poem> = authorPoemsMap[author] ?: emptyList()

    fun countsByDynasty(): List<Pair<String, Int>> {
        ensureStats(); return dynastyCache
    }

    fun allThemes(): List<Pair<String, Int>> {
        ensureStats(); return themesCache
    }

    fun allForms(): List<Pair<String, Int>> {
        ensureStats(); return formsCache
    }

    // ---------- 加载 ----------

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { optString(it) }

    private fun parsePoem(o: JSONObject): Poem = Poem(
        id = o.optString("id"),
        title = o.optString("title"),
        author = o.optString("author"),
        dynasty = o.optString("dynasty"),
        form = o.optString("form"),
        tune = if (o.has("tune")) o.optString("tune") else null,
        themes = o.optJSONArray("themes")?.toStringList() ?: emptyList(),
        body = o.optString("body"),
        translation = o.optString("translation"),
        notes = o.optJSONArray("notes")?.let { nja ->
            (0 until nja.length()).map { j ->
                val n = nja.getJSONObject(j)
                NoteItem(n.optString("w"), n.optString("n"))
            }
        } ?: emptyList(),
        background = o.optString("background"),
        famous = o.optJSONArray("famous")?.toStringList() ?: emptyList(),
        textbook = if (o.has("textbook")) o.optString("textbook") else null
    )

    private fun parsePoets(jp: JSONArray) {
        for (i in 0 until jp.length()) {
            val o = jp.getJSONObject(i)
            val poet = Poet(
                name = o.optString("name"),
                dynasty = o.optString("dynasty"),
                years = o.optString("years"),
                honor = o.optString("honor"),
                bio = o.optString("bio")
            )
            if (poet.name.isNotBlank() && !poets.containsKey(poet.name)) {
                poets[poet.name] = poet
            }
        }
    }

    private fun loadDir(ctx: Context, dir: String): List<Poem> {
        val assets = ctx.assets
        val out = ArrayList<Poem>(1024)
        val files = try {
            assets.list(dir).orEmpty().filter { it.endsWith(".json") }.sorted()
        } catch (_: Exception) {
            return out
        }
        for (f in files) {
            try {
                val root = JSONObject(assets.open("$dir/$f").bufferedReader().use { it.readText() })
                val ja = root.optJSONArray("poems")
                if (ja != null) {
                    for (i in 0 until ja.length()) {
                        val poem = parsePoem(ja.getJSONObject(i))
                        if (poem.id.isBlank() || byId.containsKey(poem.id)) continue
                        if (firstLineKey(poem) in seenKeys) continue
                        seenKeys.add(firstLineKey(poem))
                        byId[poem.id] = poem
                        out.add(poem)
                    }
                }
                root.optJSONArray("poets")?.let { parsePoets(it) }
            } catch (_: Exception) {
                // 单个数据文件损坏时跳过
            }
        }
        return out
    }

    private val seenKeys = HashSet<String>()

    private fun firstLineKey(p: Poem): String {
        val flat = p.body.replace(Regex("[\\s，。、！？；：·《》（）()\\-—]"), "")
        return p.author + "|" + flat.take(12)
    }

    private fun resortAndReplace(list: List<Poem>) {
        val sorted = list.sortedWith(compareBy({ dynastyOrder.indexOf(it.dynasty) }, { it.author }, { it.title }))
        poems.clear()
        poems.addAll(sorted) // 单次批量写入，避免 6.7 万次逐条快照写入
    }

    private fun loadFamous(ctx: Context) {
        try {
            val text = ctx.assets.open("famous.json").bufferedReader().use { it.readText() }
            val ja = JSONObject(text).optJSONArray("lines") ?: return
            for (i in 0 until ja.length()) {
                val o = ja.getJSONObject(i)
                appreciations.add(
                    FamousLine(
                        id = o.optString("id"),
                        line = o.optString("line"),
                        title = o.optString("title"),
                        author = o.optString("author"),
                        dynasty = o.optString("dynasty"),
                        poemId = o.optString("poemId"),
                        appreciation = o.optString("appreciation")
                    )
                )
            }
        } catch (_: Exception) {
        }
        val seenLines = HashSet<String>()
        appreciations.forEach { seenLines.add(it.line) }
        for (p in poems) {
            for (f in p.famous) {
                if (f in seenLines) continue
                seenLines.add(f)
                appreciations.add(
                    FamousLine(
                        id = "P-" + p.id,
                        line = f,
                        title = p.title,
                        author = p.author,
                        dynasty = p.dynasty,
                        poemId = p.id,
                        appreciation = ""
                    )
                )
            }
        }
    }

    /** 第一段：精选集，启动时加载 */
    fun init(ctx: Context) {
        if (ready) return
        synchronized(this) {
            if (ready) return
            val curated = loadDir(ctx, "poems")
            resortAndReplace(curated)
            loadFamous(ctx)
            bumpVersions()
            ready = true
        }
    }

    /** 第二段：全集语料，后台静默并入 */
    fun initBulk(ctx: Context) {
        if (!ready) return
        synchronized(this) {
            if (bulkDone) return
            val bulk = loadDir(ctx, "bulk")
            if (bulk.isNotEmpty()) {
                val all = poems.toList() + bulk
                resortAndReplace(all)
                // 新增语料的名句并入佳句池
                val seenLines = HashSet<String>()
                appreciations.forEach { seenLines.add(it.line) }
                for (p in bulk) {
                    for (f in p.famous) {
                        if (f in seenLines) continue
                        seenLines.add(f)
                        appreciations.add(
                            FamousLine("P-" + p.id, f, p.title, p.author, p.dynasty, p.id, "")
                        )
                    }
                }
                bumpVersions()
            }
            bulkDone = true
        }
    }

    @Volatile
    private var bulkDone = false

    // ---------- 查询 ----------

    fun famousLines(): List<Pair<Poem, String>> = poems.flatMap { p -> p.famous.map { p to it } }

    fun search(qRaw: String): List<Poem> {
        val q = qRaw.trim()
        if (q.isEmpty()) return emptyList()
        return poems.filter { p ->
            p.title.contains(q) || p.author.contains(q) || p.body.contains(q) || p.famous.any { it.contains(q) }
        }.sortedWith(
            compareByDescending<Poem> { it.title.contains(q) || it.author.contains(q) }
                .thenBy { dynastyOrder.indexOf(it.dynasty) }
        )
    }

    /** 好句搜索（含现代名句与用户自添加），最多返回 60 条 */
    fun searchQuotes(qRaw: String): List<FamousLine> {
        val q = qRaw.trim()
        if (q.isEmpty()) return emptyList()
        return appreciations.filter {
            it.line.contains(q) || it.title.contains(q) || it.author.contains(q)
        }.take(60)
    }

    // ---------- 搜索历史 ----------

    private const val HIST_KEY = "searchHistory"
    private const val HIST_MAX = 10

    fun searchHistory(ctx: Context): List<String> {
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        return prefs.getString(HIST_KEY, "")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
    }

    fun addSearchHistory(ctx: Context, q: String) {
        val query = q.trim()
        if (query.isEmpty()) return
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        val list = searchHistory(ctx).toMutableList()
        list.removeAll { it == query }
        list.add(0, query)
        prefs.edit().putString(HIST_KEY, list.take(HIST_MAX).joinToString("\n")).apply()
    }

    fun clearSearchHistory(ctx: Context) {
        ctx.getSharedPreferences("shici", Context.MODE_PRIVATE).edit().remove(HIST_KEY).apply()
    }

    fun findAppreciation(id: String): FamousLine? = appreciations.firstOrNull { it.id == id }

    // ---------- 今日推荐 ----------

    private fun utcDay(): Long = System.currentTimeMillis() / 86400000L

    fun todayText(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.MONTH) + 1}月${c.get(Calendar.DAY_OF_MONTH)}日"
    }

    fun dailyPoem(ctx: Context): Poem? {
        if (poems.isEmpty()) return null
        val day = utcDay()
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        prefs.getString("daily_$day", null)?.let { byId[it] }?.let { return it }
        return poems[Random(day).nextInt(poems.size)]
    }

    fun reshuffleDaily(ctx: Context): Poem? {
        if (poems.isEmpty()) return null
        val day = utcDay()
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        val currentId = prefs.getString("daily_$day", null) ?: dailyPoem(ctx)?.id
        val pool = poems.filter { it.id != currentId }
        val next = if (pool.isEmpty()) poems.first() else pool[Random(System.nanoTime()).nextInt(pool.size)]
        prefs.edit().putString("daily_$day", next.id).apply()
        return next
    }

    fun dailyFamous(ctx: Context): Pair<Poem, String>? {
        val all = famousLines()
        if (all.isEmpty()) return null
        val day = utcDay()
        return all[Random(day * 7919L).nextInt(all.size)]
    }

    /** 每日佳句：每次打开随机取一条（可手动换） */
    fun dailyAppreciation(): FamousLine? {
        if (appreciations.isEmpty()) return null
        return appreciations[Random(System.nanoTime()).nextInt(appreciations.size)]
    }

    /** 随机换一句（与当前不同） */
    fun randomAppreciation(currentId: String?): FamousLine? {
        if (appreciations.isEmpty()) return null
        val pool = appreciations.filter { it.id != currentId }
        if (pool.isEmpty()) return appreciations.first()
        return pool[Random(System.nanoTime()).nextInt(pool.size)]
    }

    // ---------- 收藏 ----------

    fun favorites(ctx: Context): Set<String> {
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        return prefs.getStringSet("favs", emptySet()) ?: emptySet()
    }

    fun isFav(ctx: Context, id: String): Boolean = favorites(ctx).contains(id)

    /** 返回切换后的收藏状态 */
    fun toggleFav(ctx: Context, id: String): Boolean {
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        val set = HashSet(prefs.getStringSet("favs", emptySet()) ?: emptySet())
        val nowFav = if (set.contains(id)) {
            set.remove(id)
            false
        } else {
            set.add(id)
            true
        }
        prefs.edit().putStringSet("favs", set).apply()
        return nowFav
    }

    fun favoritePoems(ctx: Context): List<Poem> {
        val ids = favorites(ctx)
        return poems.filter { ids.contains(it.id) }
    }

    // ---------- 佳句收藏 ----------

    fun quoteFavorites(ctx: Context): Set<String> {
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        return prefs.getStringSet("quoteFavs", emptySet()) ?: emptySet()
    }

    fun isQuoteFav(ctx: Context, id: String): Boolean = quoteFavorites(ctx).contains(id)

    /** 返回切换后的收藏状态 */
    fun toggleQuoteFav(ctx: Context, id: String): Boolean {
        val prefs = ctx.getSharedPreferences("shici", Context.MODE_PRIVATE)
        val set = HashSet(prefs.getStringSet("quoteFavs", emptySet()) ?: emptySet())
        val nowFav = if (set.contains(id)) {
            set.remove(id)
            false
        } else {
            set.add(id)
            true
        }
        prefs.edit().putStringSet("quoteFavs", set).apply()
        return nowFav
    }

    fun favoriteQuotes(ctx: Context): List<FamousLine> {
        val ids = quoteFavorites(ctx)
        return appreciations.filter { ids.contains(it.id) }
    }
}
