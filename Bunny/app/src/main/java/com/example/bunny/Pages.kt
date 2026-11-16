package com.example.bunny

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max

object Preview {
    private val pose = Pose()

    fun draw(c: Canvas, item: Item, rect: RectF) {
        val s = rect.height() * 0.56f
        val cx = rect.centerX(); val by = rect.bottom - rect.height() * 0.04f
        when (item.cat) {
            Items.HAT -> BunnyArt.draw(c, cx, by, s, pose, item.id, "", "")
            Items.OUTFIT -> BunnyArt.draw(c, cx, by, s, pose, "", item.id, "")
            Items.ACC -> BunnyArt.draw(c, cx, by, s, pose, "", "", item.id)
            Items.WALL -> {
                val r = RectF(rect.left + rect.width() * 0.08f, rect.top + rect.height() * 0.05f, rect.right - rect.width() * 0.08f, rect.bottom - rect.height() * 0.05f)
                Rooms.drawWall(c, r, item.id)
                D.s(C.outline, 3f); c.drawRect(r, D.p)
            }
            Items.FLOOR -> {
                val r = RectF(rect.left + rect.width() * 0.08f, rect.top + rect.height() * 0.05f, rect.right - rect.width() * 0.08f, rect.bottom - rect.height() * 0.05f)
                Rooms.drawFloor(c, r, item.id)
                D.s(C.outline, 3f); c.drawRect(r, D.p)
            }
            else -> Rooms.drawDecor(c, item.id, cx, rect.centerY() + rect.height() * 0.1f, rect.width() * 0.55f)
        }
    }
}

abstract class GridScreen(g: BunnyView) : Screen(g) {
    protected var tab = 0
    protected var time = 0f
    private var scrollY = 0f
    private var maxScroll = 0f
    private var moved = 0f
    private val tabRects = ArrayList<RectF>()
    protected val headerRect = RectF()
    private val gridRect = RectF()
    private val cellRects = ArrayList<RectF>()
    private var cached: List<Item?> = emptyList()

    abstract val tabNames: List<String>
    abstract fun headerH(r: RectF): Float
    abstract fun drawHeader(c: Canvas, r: RectF)
    abstract fun cells(): List<Item?>
    abstract fun drawCell(c: Canvas, cell: RectF, item: Item?)
    abstract fun tapCell(item: Item?)
    open fun headerTap(x: Float, y: Float): Boolean = false

    override fun update(dt: Float) { time += dt; updFx(dt) }

    override fun draw(c: Canvas, r: RectF) {
        D.f(D.mix(C.cream, C.pink, 0.2f)); c.drawRect(r, D.p)
        headerRect.set(r.left, r.top, r.right, r.top + headerH(r))
        drawHeader(c, headerRect)
        // separadores
        val n = tabNames.size
        val th = r.height() * 0.06f
        val ty = headerRect.bottom
        tabRects.clear()
        val tw = r.width() / n
        for (i in 0 until n) {
            val tr = RectF(r.left + i * tw + 4f, ty, r.left + (i + 1) * tw - 4f, ty + th)
            tabRects.add(tr)
            D.rrect(c, tr, th * 0.4f, if (i == tab) C.pink else C.white, C.outline, 3f)
            D.text(c, tabNames[i], tr.centerX(), tr.centerY() + th * 0.15f, min2(th * 0.36f, tw * 0.17f), C.ink)
        }
        gridRect.set(r.left, ty + th + r.height() * 0.01f, r.right, r.bottom)
        cached = cells()
        val cols = 3
        val m = r.width() * 0.03f
        val cw = (r.width() - (cols + 1) * m) / cols
        val ch = cw * 1.2f
        val rows = (cached.size + cols - 1) / cols
        val total = rows * (ch + m) + m
        maxScroll = max(0f, total - gridRect.height())
        scrollY = scrollY.coerceIn(0f, maxScroll)
        cellRects.clear()
        c.save()
        c.clipRect(gridRect)
        for (i in cached.indices) {
            val col = i % cols; val row = i / cols
            val l = r.left + m + col * (cw + m)
            val t = gridRect.top + m + row * (ch + m) - scrollY
            val cell = RectF(l, t, l + cw, t + ch)
            cellRects.add(cell)
            if (cell.bottom > gridRect.top && cell.top < gridRect.bottom) drawCell(c, cell, cached[i])
        }
        if (cached.isEmpty()) D.text(c, "Ainda não tens nada aqui.", r.centerX(), gridRect.top + r.height() * 0.1f, r.height() * 0.03f, C.ink)
        c.restore()
        drawFx(c)
    }

    private fun min2(a: Float, b: Float) = if (a < b) a else b

    override fun down(x: Float, y: Float, r: RectF) {
        moved = 0f
        if (headerRect.contains(x, y) && headerTap(x, y)) { moved = 999f; return }
        for (i in tabRects.indices) if (tabRects[i].contains(x, y)) { tab = i; scrollY = 0f; moved = 999f; return }
    }

    override fun move(x: Float, y: Float, dx: Float, dy: Float, r: RectF) {
        moved += abs(dx) + abs(dy)
        if (gridRect.contains(x, y)) scrollY = (scrollY - dy).coerceIn(0f, maxScroll)
    }

    override fun up(x: Float, y: Float, r: RectF) {
        if (moved > 24f || !gridRect.contains(x, y)) return
        for (i in cellRects.indices) if (cellRects[i].contains(x, y) && i < cached.size) { tapCell(cached[i]); return }
    }

    protected fun cardBase(c: Canvas, cell: RectF, highlight: Boolean) {
        D.rrect(c, cell, cell.width() * 0.1f, if (highlight) C.mint else C.white, if (highlight) C.greenD else C.outline, if (highlight) 6f else 3f)
    }

    protected fun previewRect(cell: RectF) =
        RectF(cell.left + cell.width() * 0.05f, cell.top + cell.height() * 0.04f, cell.right - cell.width() * 0.05f, cell.top + cell.height() * 0.68f)

    protected fun nameText(c: Canvas, cell: RectF, s: String) {
        D.text(c, s, cell.centerX(), cell.top + cell.height() * 0.78f, cell.height() * 0.1f, C.ink)
    }
}

// ================================================================= LOJA
class ShopScreen(g: BunnyView) : GridScreen(g) {
    override val tabNames: List<String> get() = Items.tabNames
    override fun headerH(r: RectF) = r.height() * 0.13f

    override fun drawHeader(c: Canvas, r: RectF) {
        Icons.draw(c, Icons.BAG, r.left + r.width() * 0.14f, r.centerY(), r.height() * 0.3f)
        D.text(c, "Loja do Bunny", r.centerX() + r.width() * 0.05f, r.centerY() - r.height() * 0.02f, r.height() * 0.3f, C.ink)
        D.text(c, "Compra com as tuas moedas", r.centerX() + r.width() * 0.05f, r.centerY() + r.height() * 0.25f, r.height() * 0.17f, C.ink)
    }

    override fun cells(): List<Item?> = Items.byCat(tab).filter { it.price > 0 }

    override fun drawCell(c: Canvas, cell: RectF, item: Item?) {
        val it = item ?: return
        val owned = Save.has(it.id)
        cardBase(c, cell, owned)
        Preview.draw(c, it, previewRect(cell))
        nameText(c, cell, it.name)
        val chip = RectF(cell.left + cell.width() * 0.15f, cell.top + cell.height() * 0.82f, cell.right - cell.width() * 0.15f, cell.bottom - cell.height() * 0.03f)
        if (owned) D.button(c, chip, "Teu", C.mint, C.ink, chip.height() * 0.5f)
        else {
            D.rrect(c, chip, chip.height() * 0.4f, C.butter, C.gold, 3f)
            Icons.draw(c, Icons.COIN, chip.left + chip.height() * 0.55f, chip.centerY(), chip.height() * 0.3f)
            D.text(c, it.price.toString(), chip.centerX() + chip.height() * 0.3f, chip.centerY() + chip.height() * 0.17f, chip.height() * 0.5f, C.ink)
        }
    }

    override fun tapCell(item: Item?) {
        val it = item ?: return
        when {
            Save.has(it.id) -> g.say("Já tens este item!")
            Save.coins >= it.price -> { Save.coins -= it.price; Save.owned.add(it.id); g.say("Comprado! Vê em Roupas/Decorar"); Save.save() }
            else -> g.say("Faltam ${it.price - Save.coins} moedas — joga os mini-jogos!")
        }
    }
}

// ================================================================= ROUPAS
class WardrobeScreen(g: BunnyView) : GridScreen(g) {
    private val pose = Pose()
    override val tabNames: List<String> get() = listOf("Chapéus", "Roupa", "Acessórios")
    override fun headerH(r: RectF) = r.height() * 0.4f

    override fun update(dt: Float) { super.update(dt); pose.t = time }

    override fun drawHeader(c: Canvas, r: RectF) {
        D.circle(c, r.centerX(), r.centerY() + r.height() * 0.1f, r.height() * 0.42f, D.alpha(C.white, 200), C.outline, 4f)
        pose.mood = 0
        BunnyArt.draw(c, r.centerX(), r.bottom - r.height() * 0.05f, r.height() * 0.5f, pose, Save.hat, Save.outfit, Save.acc)
        D.text(c, "Guarda-roupa", r.left + r.width() * 0.04f, r.top + r.height() * 0.1f, r.height() * 0.09f, C.ink, Paint.Align.LEFT)
    }

    private fun equipped(): String = when (tab) { 0 -> Save.hat; 1 -> Save.outfit; else -> Save.acc }

    override fun cells(): List<Item?> {
        val l = ArrayList<Item?>()
        l.add(null)
        l.addAll(Items.byCat(tab).filter { Save.has(it.id) })
        return l
    }

    override fun drawCell(c: Canvas, cell: RectF, item: Item?) {
        val on = (item?.id ?: "") == equipped()
        cardBase(c, cell, on)
        if (item == null) {
            D.line(c, cell.centerX() - cell.width() * 0.15f, cell.top + cell.height() * 0.25f, cell.centerX() + cell.width() * 0.15f, cell.top + cell.height() * 0.5f, 6f, C.outline)
            D.line(c, cell.centerX() + cell.width() * 0.15f, cell.top + cell.height() * 0.25f, cell.centerX() - cell.width() * 0.15f, cell.top + cell.height() * 0.5f, 6f, C.outline)
            nameText(c, cell, "Nenhum")
        } else {
            Preview.draw(c, item, previewRect(cell))
            nameText(c, cell, item.name)
        }
        if (on) D.text(c, "Em uso", cell.centerX(), cell.bottom - cell.height() * 0.06f, cell.height() * 0.09f, C.greenD)
    }

    override fun tapCell(item: Item?) {
        val id = item?.id ?: ""
        when (tab) { 0 -> Save.hat = id; 1 -> Save.outfit = id; else -> Save.acc = id }
        Save.save()
    }
}

// ================================================================= DECORAR
class DecorScreen(g: BunnyView) : GridScreen(g) {
    private var room = 0
    private val roomRects = Array(3) { RectF() }
    override val tabNames: List<String> get() = listOf("Paredes", "Chão", "Objetos")
    override fun headerH(r: RectF) = r.height() * 0.4f

    override fun drawHeader(c: Canvas, r: RectF) {
        val th = r.height() * 0.15f
        val tw = r.width() / 3f
        for (i in 0 until 3) {
            roomRects[i].set(r.left + i * tw + 4f, r.top + 4f, r.left + (i + 1) * tw - 4f, r.top + th)
            D.rrect(c, roomRects[i], th * 0.4f, if (i == room) C.lav else C.white, C.outline, 3f)
            D.text(c, Rooms.names[i], roomRects[i].centerX(), roomRects[i].centerY() + th * 0.12f, th * 0.42f, C.ink)
        }
        val pr = RectF(r.left + r.width() * 0.04f, r.top + th + 10f, r.right - r.width() * 0.04f, r.bottom - 4f)
        c.save(); c.clipRect(pr)
        Rooms.draw(c, pr, room, 0f, 0f)
        c.restore()
        D.s(C.outline, 5f); c.drawRect(pr, D.p)
    }

    override fun headerTap(x: Float, y: Float): Boolean {
        for (i in 0 until 3) if (roomRects[i].contains(x, y)) { room = i; return true }
        return false
    }

    private fun cat() = Items.WALL + tab

    override fun cells(): List<Item?> = Items.byCat(cat()).filter { Save.has(it.id) }

    private fun inUse(it: Item): Boolean = when (it.cat) {
        Items.WALL -> Save.wall[room] == it.id
        Items.FLOOR -> Save.floor[room] == it.id
        else -> Save.decor.contains("${it.id}@$room")
    }

    override fun drawCell(c: Canvas, cell: RectF, item: Item?) {
        val it = item ?: return
        val on = inUse(it)
        cardBase(c, cell, on)
        Preview.draw(c, it, previewRect(cell))
        nameText(c, cell, it.name)
        if (on) D.text(c, "Em uso", cell.centerX(), cell.bottom - cell.height() * 0.06f, cell.height() * 0.09f, C.greenD)
    }

    override fun tapCell(item: Item?) {
        val it = item ?: return
        when (it.cat) {
            Items.WALL -> Save.wall[room] = it.id
            Items.FLOOR -> Save.floor[room] = it.id
            else -> { val k = "${it.id}@$room"; if (!Save.decor.remove(k)) Save.decor.add(k) }
        }
        Save.save()
    }
}
