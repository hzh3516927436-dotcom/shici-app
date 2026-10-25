package com.zhonghua.shici.data

data class NoteItem(val w: String, val n: String)

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val form: String,
    val tune: String?,
    val themes: List<String>,
    val body: String,
    val translation: String,
    val notes: List<NoteItem>,
    val background: String,
    val famous: List<String>,
    val textbook: String?
) {
    val bodyLines: List<String> get() = body.split("\n")
    val meta: String get() = buildString {
        append("〔$dynasty〕$author")
        if (!tune.isNullOrBlank()) append(" · $tune")
        append(" · $form")
    }
}

data class Poet(
    val name: String,
    val dynasty: String,
    val years: String,
    val honor: String,
    val bio: String
)
