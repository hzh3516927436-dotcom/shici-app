package com.zhonghua.shici.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 用户自添加内容：诗词/文章/好句，本地持久化，可被搜索与收藏 */
object UserStore {

    val poems = mutableStateListOf<Poem>()
    val quotes = mutableStateListOf<FamousLine>()

    @Volatile
    private var loaded = false

    fun init(ctx: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val f = File(ctx.filesDir, "user_content.json")
            if (f.exists()) {
                try {
                    val root = JSONObject(f.readText())
                    root.optJSONArray("poems")?.let { ja ->
                        for (i in 0 until ja.length()) {
                            val o = ja.getJSONObject(i)
                            poems.add(
                                Poem(
                                    id = o.optString("id"),
                                    title = o.optString("title"),
                                    author = o.optString("author"),
                                    dynasty = o.optString("dynasty"),
                                    form = o.optString("form"),
                                    tune = if (o.has("tune")) o.optString("tune") else null,
                                    themes = emptyList(),
                                    body = o.optString("body"),
                                    translation = o.optString("translation"),
                                    notes = emptyList(),
                                    background = o.optString("background"),
                                    famous = emptyList(),
                                    textbook = null
                                )
                            )
                        }
                    }
                    root.optJSONArray("quotes")?.let { ja ->
                        for (i in 0 until ja.length()) {
                            val o = ja.getJSONObject(i)
                            quotes.add(
                                FamousLine(
                                    id = o.optString("id"),
                                    line = o.optString("line"),
                                    title = o.optString("title"),
                                    author = o.optString("author"),
                                    dynasty = "我的",
                                    poemId = o.optString("poemId"),
                                    appreciation = o.optString("appreciation")
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                }
            }
            loaded = true
        }
    }

    fun addPoem(ctx: Context, title: String, author: String, dynasty: String, form: String, body: String, note: String): Poem {
        val p = Poem(
            id = "U" + System.currentTimeMillis(),
            title = title.trim(),
            author = author.trim().ifEmpty { "佚名" },
            dynasty = dynasty.trim().ifEmpty { "我的" },
            form = form.trim().ifEmpty { "自录" },
            tune = null,
            themes = listOf("我的"),
            body = body.trim(),
            translation = "",
            notes = emptyList(),
            background = note.trim(),
            famous = emptyList(),
            textbook = null
        )
        poems.add(p)
        persist(ctx)
        return p
    }

    fun addQuote(ctx: Context, line: String, title: String, author: String, appreciation: String): FamousLine {
        val q = FamousLine(
            id = "UF" + System.currentTimeMillis(),
            line = line.trim(),
            title = title.trim(),
            author = author.trim().ifEmpty { "佚名" },
            dynasty = "我的",
            poemId = "",
            appreciation = appreciation.trim()
        )
        quotes.add(q)
        persist(ctx)
        return q
    }

    fun removePoem(ctx: Context, id: String) {
        poems.removeAll { it.id == id }
        persist(ctx)
    }

    fun removeQuote(ctx: Context, id: String) {
        quotes.removeAll { it.id == id }
        persist(ctx)
    }

    private fun persist(ctx: Context) {
        try {
            val root = JSONObject()
            val ja = JSONArray()
            for (p in poems) {
                ja.put(
                    JSONObject()
                        .put("id", p.id).put("title", p.title).put("author", p.author)
                        .put("dynasty", p.dynasty).put("form", p.form).put("body", p.body)
                        .put("background", p.background)
                )
            }
            root.put("poems", ja)
            val jq = JSONArray()
            for (q in quotes) {
                jq.put(
                    JSONObject()
                        .put("id", q.id).put("line", q.line).put("title", q.title)
                        .put("author", q.author).put("appreciation", q.appreciation)
                )
            }
            root.put("quotes", jq)
            File(ctx.filesDir, "user_content.json").writeText(root.toString())
        } catch (_: Exception) {
        }
    }

    /** 把用户内容并入主仓库（可搜索/可收藏/进每日佳句池），返回是否有变化 */
    fun syncIntoRepo(): Boolean {
        var changed = false
        for (p in poems) {
            if (!Repo.byId.containsKey(p.id)) {
                Repo.byId[p.id] = p
                Repo.poems.add(p)
                changed = true
            }
        }
        val seen = Repo.appreciations.map { it.id }.toHashSet()
        for (q in quotes) {
            if (q.id !in seen) {
                Repo.appreciations.add(q)
                changed = true
            }
        }
        if (changed) Repo.bumpUserVersion()
        return changed
    }
}
