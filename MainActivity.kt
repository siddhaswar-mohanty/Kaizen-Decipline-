package com.sidhu.kaizen

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val bg = Color.rgb(245, 242, 233)
    private val card = Color.WHITE
    private val ink = Color.rgb(39, 45, 39)
    private val muted = Color.rgb(111, 117, 107)
    private val green = Color.rgb(75, 112, 83)
    private val prefs by lazy { getSharedPreferences("kaizen_data", Context.MODE_PRIVATE) }
    private val habits = listOf(
        Habit("KAIZEN", "The One-Minute Rule", "Start tiny. One push-up, one sentence, one minute of coding. Small actions make starting easier.", "Do one tiny improvement today."),
        Habit("IKIGAI", "A Reason to Wake Up", "Connect today's effort to a meaningful reason. Write why your learning and goals matter.", "Write your reason for today's effort."),
        Habit("HARA HACHI BU", "Eat Mindfully", "Eat slowly and stop when comfortably satisfied. Do not undereat; use this as a mindful-eating reminder.", "Eat one meal slowly and mindfully."),
        Habit("ANCHORED FOCUS", "Ritualized Focus", "Try 25 minutes of focused work followed by a 5-minute break. Begin with the same small ritual.", "Complete one 25-minute focus session."),
        Habit("SEIRI & SEITON", "Clear Space", "Make your bed, clear your desk, and keep only what you need nearby.", "Tidy your desk or room for 5 minutes."),
        Habit("KINTSUGI", "Finish Imperfectly", "Finish a small task instead of waiting for perfection. Treat mistakes as part of learning.", "Complete one task without over-polishing."),
        Habit("WABI-SABI", "Act Before Perfect", "Start with the tools and conditions you already have. Improve after you begin.", "Take one imperfect first step.")
    )
    private val routines = listOf(
        "Morning: wake up, make your bed, drink water, plan the day",
        "Movement: 15–20 minutes of walking, stretching, or suitable exercise",
        "Learning: two 25-minute Java / DSA focus sessions",
        "College: attend class, take short notes, revise one concept",
        "Building: spend one focused block on a practical software project",
        "Evening: walk, exercise, or spend time with family",
        "Night: review progress, prepare tomorrow's tasks, keep a consistent sleep time"
    )
    private var root: LinearLayout? = null
    private var selectedTab = "Today"

    data class Habit(val name: String, val title: String, val detail: String, val action: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen()
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun keyFor(date: Date): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    private fun completed(key: String): MutableSet<Int> {
        val raw = prefs.getString("done_$key", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getInt(it) }.toMutableSet()
    }
    private fun saveDone(key: String, set: Set<Int>) {
        val arr = JSONArray()
        set.sorted().forEach { arr.put(it) }
        prefs.edit().putString("done_$key", arr.toString()).apply()
    }
    private fun routineDone(key: String): MutableSet<Int> {
        val arr = JSONArray(prefs.getString("routine_$key", "[]") ?: "[]")
        return (0 until arr.length()).map { arr.getInt(it) }.toMutableSet()
    }
    private fun saveRoutine(key: String, set: Set<Int>) {
        val arr = JSONArray()
        set.sorted().forEach { arr.put(it) }
        prefs.edit().putString("routine_$key", arr.toString()).apply()
    }

    private fun showScreen() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
        }
        root = page
        scroll.addView(page)
        setContentView(scroll)

        page.addView(text("KAIZEN", 28, ink, true))
        page.addView(text("YOUR DAILY DISCIPLINE COMPANION  ·  七", 11, muted, true))
        page.addView(space(16))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Today", "Habits", "30 Days", "Routine").forEach { tab ->
            val b = Button(this).apply {
                text = tab
                textSize = 11f
                isAllCaps = false
                setTextColor(if (selectedTab == tab) Color.WHITE else ink)
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(if (selectedTab == tab) green else Color.rgb(232, 229, 218)))
                setOnClickListener { selectedTab = tab; showScreen() }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, dp(44), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        page.addView(nav)
        page.addView(space(16))
        when (selectedTab) {
            "Today" -> today(page)
            "Habits" -> habitsScreen(page)
            "30 Days" -> monthScreen(page)
            "Routine" -> routineScreen(page)
        }
        page.addView(space(12))
        page.addView(text("Small steps. Every day.", 13, muted, true).apply { gravity = Gravity.CENTER })
    }

    private fun today(p: LinearLayout) {
        val done = completed(todayKey())
        val routine = routineDone(todayKey())
        val pct = done.size * 100 / habits.size
        sectionCard(p) {
            addView(text("TODAY · ${todayKey()}", 12, muted, true))
            addView(space(6))
            addView(text("$pct%", 38, ink, true))
            addView(text("${done.size} of 7 habits completed", 14, muted))
            addView(progress(done.size, 7))
            addView(space(8))
            addView(text("Streak: ${streak()} days", 15, green, true))
        }
        p.addView(space(12))
        p.addView(text("TODAY'S SEVEN HABITS", 12, muted, true))
        habits.forEachIndexed { i, h ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(8), dp(10))
                setBackgroundColor(card)
            }
            val cb = CheckBox(this).apply {
                text = "${h.name} — ${h.title}"
                textSize = 13f
                setTextColor(ink)
                isChecked = done.contains(i)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                setOnCheckedChangeListener { _, checked ->
                    val updated = completed(todayKey())
                    if (checked) updated.add(i) else updated.remove(i)
                    saveDone(todayKey(), updated)
                    showScreen()
                }
            }
            row.addView(cb, LinearLayout.LayoutParams(-1, -2))
            p.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) })
        }
        p.addView(space(12))
        p.addView(text("DAILY ROUTINE", 12, muted, true))
        routines.forEachIndexed { i, item ->
            val cb = CheckBox(this).apply {
                text = item
                textSize = 13f
                setTextColor(ink)
                isChecked = routine.contains(i)
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                setOnCheckedChangeListener { _, checked ->
                    val updated = routineDone(todayKey())
                    if (checked) updated.add(i) else updated.remove(i)
                    saveRoutine(todayKey(), updated)
                }
            }
            sectionCard(p) { addView(cb) }
        }
    }

    private fun habitsScreen(p: LinearLayout) {
        p.addView(text("THE SEVEN JAPANESE HABITS", 12, muted, true))
        habits.forEachIndexed { i, h ->
            sectionCard(p) {
                addView(text("${i + 1}. ${h.name}", 14, green, true))
                addView(text(h.title, 18, ink, true))
                p.addView(space(5))
                addView(text(h.detail, 14, muted))
                p.addView(space(7))
                addView(text("TODAY'S ACTION", 10, green, true))
                addView(text(h.action, 14, ink))
            }
            addView(space(8))
        }
        p.addView(text("These are practical habit prompts inspired by the reel, not a guarantee that the habits will eliminate laziness.", 12, muted))
    }

    private fun monthScreen(p: LinearLayout) {
        p.addView(text("30-DAY CONSISTENCY TRACKER", 12, muted, true))
        p.addView(text("Your habit history is saved on this device.", 13, muted))
        p.addView(space(10))
        val today = Calendar.getInstance()
        for (offset in 29 downTo 0) {
            val c = Calendar.getInstance().apply { time = today.time; add(Calendar.DAY_OF_YEAR, -offset) }
            val key = keyFor(c.time)
            val count = completed(key).size
            val date = SimpleDateFormat("EEE, dd MMM", Locale.US).format(c.time)
            sectionCard(p) {
                addView(text(date, 13, ink, true))
                addView(text("$count / 7 habits", 12, muted))
                addView(progress(count, 7))
            }
            p.addView(space(5))
        }
    }

    private fun routineScreen(p: LinearLayout) {
        p.addView(text("A FLEXIBLE DAILY PLAN", 12, muted, true))
        p.addView(text("Use this as a starting point. Shift the timing around your classes and responsibilities.", 14, muted))
        p.addView(space(10))
        routines.forEachIndexed { i, item ->
            sectionCard(p) {
                addView(text("STEP ${i + 1}", 10, green, true))
                addView(text(item, 15, ink, true))
            }
            p.addView(space(7))
        }
        sectionCard(p) {
            addView(text("THE THREE RULES", 14, green, true))
            addView(text("1. Never miss twice.\n2. Don't wait for motivation.\n3. Consistency over intensity.", 14, ink))
        }
    }

    private fun streak(): Int {
        var n = 0
        val cal = Calendar.getInstance()
        // A streak day counts when all seven habits are checked.
        while (true) {
            val count = completed(keyFor(cal.time)).size
            if (count == 7) { n++; cal.add(Calendar.DAY_OF_YEAR, -1) } else break
        }
        return n
    }

    private fun sectionCard(parent: LinearLayout, content: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            setBackgroundColor(card)
            elevation = dp(1).toFloat()
            content()
        }
        parent.addView(box, LinearLayout.LayoutParams(-1, -2))
    }
    private fun progress(value: Int, max: Int): ProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
        this.max = max
        progress = value
        progressTintList = android.content.res.ColorStateList.valueOf(green)
        progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(229, 229, 219))
        layoutParams = LinearLayout.LayoutParams(-1, dp(8)).apply { topMargin = dp(8) }
    }
    private fun text(s: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun space(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
