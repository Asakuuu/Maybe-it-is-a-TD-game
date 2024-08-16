package com.example.myapplication.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myapplication.R
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import java.util.*

class GridView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val numColumns = 10
    private val numRows = 11
    private val cellWidth: Float = width.toFloat() / numColumns
    private val cellHeight: Float = height.toFloat() / numRows
    private val paint: Paint = Paint()
    private val handler = Handler(Looper.getMainLooper())

    // 圖片
    private var monsterBitmap: Bitmap
    private var heartBitmap: Bitmap
    private var cannonBitmap: Bitmap
    private var startBitmap: Bitmap
    private var endBitmap: Bitmap
    private var monster: Bitmap
    private var heart: Bitmap
    private var cannon: Bitmap
    private var start: Bitmap
    private var end: Bitmap

    private var road = arrayOf(
        Pair(-13, 3), Pair(-12, 3), Pair(-11, 3), Pair(-10, 3), Pair(-9, 3), Pair(-8, 3),
        Pair(-7, 3), Pair(-6, 3), Pair(-5, 3), Pair(-4, 3), Pair(-3, 3), Pair(-2, 3), Pair(-1, 3),
        Pair(0, 3), Pair(0, 2), Pair(0, 1), Pair(1, 1), Pair(2, 1), Pair(2, 2), Pair(3, 2),
        Pair(4, 2), Pair(5, 2), Pair(5, 3), Pair(6, 3), Pair(7, 3), Pair(8, 3), Pair(9, 3),
        Pair(9, 4), Pair(9, 5), Pair(8, 5), Pair(7, 5), Pair(7, 6), Pair(7, 7), Pair(6, 7),
        Pair(5, 7), Pair(4, 7), Pair(4, 6), Pair(4, 5), Pair(3, 5), Pair(2, 5), Pair(1, 5),
        Pair(1, 6), Pair(1, 7), Pair(1, 8), Pair(0, 8), Pair(0, 9), Pair(0, 10), Pair(1, 10),
        Pair(2, 10), Pair(3, 10), Pair(3, 9), Pair(4, 9), Pair(5, 9), Pair(6, 9), Pair(6, 10),
        Pair(7, 10), Pair(8, 10), Pair(9, 10), Pair(9, 9), Pair(9, 8)
    )
    private var tower = arrayOf(
        Pair(3, 3), Pair(5, 1), Pair(8, 2), Pair(8, 6), Pair(8, 9), Pair(0, 6), Pair(3, 8),
        Pair(1, 9), Pair(5, 10), Pair(5, 5)
    )

    //怪物
    private var monsterIndex = mutableListOf(0)
    private var monsterPosition = mutableListOf<Pair<Float, Float>>()
    private var monsterAllHealth = 3
    private var monsterHealth = mutableListOf(monsterAllHealth)

    //砲塔
    private var visibleTowers = mutableSetOf<Pair<Int, Int>>()
    private val unicornEntryCounts = mutableMapOf<Pair<Int, Int>, Int>()
    private val unicornRangePaint = Paint().apply {
        color = Color.argb(50, 255, 0, 0) // 紅色，半透明
        style = Paint.Style.FILL_AND_STROKE // 填充並描邊
        strokeWidth = 2f // 線條寬度
    }

    //點擊
    private var lastClickTime: Long = 0
    private var lastClickedPosition: Pair<Int, Int>? = null

    // 玩家血量
    private var endHealth = 3

    // 波數
    private var currentWave = 1
    private var lastWaveTime = System.currentTimeMillis()

    // 碎片
    private var money = 30
    private var moneyTimer: Timer? = null

    // 波數訊息
    private var waveMessage: String? = null
    private val wavePaint = Paint().apply {
        color = Color.parseColor("#8B0000")
        textSize = 100f
        textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // 砲臺訊息
    private var cannonMessage: String? = null
    private val cannonPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = 80f
        textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // 碎片訊息
    private var moneyMessage: String? = null
    private val moneyPaint = Paint().apply {
        color = Color.parseColor("#FF0000")
        textSize = 100f
        textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
        alpha = (255 * 0.75).toInt()
    }

    private var isGameOver = false

    init {
        startCountdown(5)
        startMovingMonster()
        startMoneyTimer()

        monsterBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.monster)
        heartBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.heart)
        cannonBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tower)
        startBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.start)
        endBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.end)
        // 缩放 Bitmap
        monster = Bitmap.createScaledBitmap(monsterBitmap, 100, 100, false)
        heart = Bitmap.createScaledBitmap(heartBitmap, 150, 150, false)
        cannon = Bitmap.createScaledBitmap(cannonBitmap, 120, 180, false)
        start = Bitmap.createScaledBitmap(startBitmap, 100, 150, false)
        end = Bitmap.createScaledBitmap(endBitmap, 100, 180, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = 2f

        drawGrid(canvas)

        visibleTowers.forEach { towerPosition ->
            val (col, row) = towerPosition

            // 绘制独角兽守护者的影响范围矩形
            val cellWidth = width.toFloat() / numColumns
            val cellHeight = height.toFloat() / numRows
            val centerX = (col + 0.5f) * cellWidth
            val centerY = (row + 0.5f) * cellHeight
            val halfWidth = 1.5f * cellWidth
            val halfHeight = 1.5f * cellHeight
            val startX = centerX - halfWidth
            val startY = centerY - halfHeight
            val endX = centerX + halfWidth
            val endY = centerY + halfHeight
            val rect = RectF(startX, startY, endX, endY)
            canvas.drawRect(rect, unicornRangePaint)

            drawTower(canvas, towerPosition)
            val entryCount = unicornEntryCounts.getOrDefault(towerPosition, 20)
            drawEntryCount(canvas, towerPosition, entryCount)
        }

        drawUp(canvas)

        drawWaveAndHearts(canvas)

        drawSE(canvas)

        drawMonster(canvas)

        monsterHealth.forEachIndexed { index, health ->
            val (col, row) = road[monsterIndex[index]]
            val cellWidth = width.toFloat() / numColumns
            val cellHeight = height.toFloat() / numRows
            val x = col * cellWidth + cellWidth / 3
            val y = row * cellHeight + cellHeight / 6
            paint.color = Color.parseColor("#930000")
            paint.textSize = 30f
            canvas.drawText("$health", x, y, paint)
        }

        for (position in visibleTowers) {
            drawTower(canvas, position)
        }

        waveMessage?.let {
            val textBounds = Rect()
            wavePaint.getTextBounds(it, 0, it.length, textBounds)
            val padding = 20
            val backgroundLeft = (width / 2f) - (textBounds.width() / 2f) - padding
            val backgroundTop = (height.toFloat() / 2f) - (textBounds.height() / 2f) - padding
            val backgroundRight = (width / 2f) + (textBounds.width() / 2f) + padding
            val backgroundBottom = (height.toFloat() / 2f) + (textBounds.height() / 2f) + padding

            canvas.drawRect(
                backgroundLeft,
                backgroundTop,
                backgroundRight,
                backgroundBottom,
                backgroundPaint
            )

            val x = width / 2f
            val y = height.toFloat() / 2f + textBounds.height() / 2f
            canvas.drawText(it, x, y, wavePaint)
        }

        cannonMessage?.let {
            val textBounds = Rect()
            cannonPaint.getTextBounds(it, 0, it.length, textBounds)
            val padding = 20
            val backgroundLeft = (width / 2f) - (textBounds.width() / 2f) - padding
            val backgroundTop = (height.toFloat() - textBounds.height() * 4) - padding
            val backgroundRight = (width / 2f) + (textBounds.width() / 2f) + padding
            val backgroundBottom = (height.toFloat() - textBounds.height() * 4) + textBounds.height() + padding

            // 繪製白色背景框
            canvas.drawRect(
                backgroundLeft,
                backgroundTop,
                backgroundRight,
                backgroundBottom,
                backgroundPaint
            )

            val x = width / 2f
            val y = height.toFloat() - textBounds.height() * 4 + textBounds.height()
            canvas.drawText(it, x, y, cannonPaint)
        }

        moneyMessage?.let {
            val textBounds = Rect()
            moneyPaint.getTextBounds(it, 0, it.length, textBounds)
            val padding = 20
            val backgroundLeft = (width / 2f) - (textBounds.width() / 2f) - padding
            val backgroundTop = (height.toFloat() - textBounds.height() * 8) - padding
            val backgroundRight = (width / 2f) + (textBounds.width() / 2f) + padding
            val backgroundBottom = (height.toFloat() - textBounds.height() * 8) + textBounds.height() + padding

            // 繪製白色背景框
            canvas.drawRect(
                backgroundLeft,
                backgroundTop,
                backgroundRight,
                backgroundBottom,
                backgroundPaint
            )

            val x = width / 2f
            val y = height.toFloat() - textBounds.height() * 8 + textBounds.height()
            canvas.drawText(it, x, y, moneyPaint)
        }
    }

    private fun drawWaveAndHearts(canvas: Canvas) {

        paint.color = Color.GRAY
        paint.textSize = 60f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("第 ${currentWave} 波　 夢境碎片 : $money", 30f, 110f, paint)

        val heartWidth = 120
        for (i in 0 until endHealth) {
            val heartX = width - i * (heartWidth - 8) - 150
            val heartY = 12
            canvas.drawBitmap(heart, heartX.toFloat(), heartY.toFloat(), paint)
        }
    }

    private fun startMoneyTimer() {
        moneyTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    money += 5
                    postInvalidate()
                }
            }, 0, 3000)
        }
    }

    private fun showWaveMessage(message: String) {
        waveMessage = message
        postInvalidate() // 重绘视图
        // 5秒后隐藏消息
        handler.postDelayed({
            waveMessage = null
            postInvalidate() // 重绘视图
        }, 5000)
    }

    private fun showCannonMessage(message: String) {
        cannonMessage = message
        postInvalidate() // 重绘视图
        // 5秒后隐藏消息
        handler.postDelayed({
            cannonMessage = null
            postInvalidate() // 重绘视图
        }, 1000)
    }

    private fun showMoneyMessage(message: String) {
        moneyMessage = message
        postInvalidate() // 重绘视图
        // 5秒后隐藏消息
        handler.postDelayed({
            moneyMessage = null
            postInvalidate() // 重绘视图
        }, 1000)
    }

    fun startCountdown(timeInSeconds: Int) {
        var countdown = timeInSeconds
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (countdown > 0) {
                    showWaveMessage("下一波將在${countdown}秒後襲來")
                    countdown--
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun drawSE(canvas: Canvas) {

        val startPosition: Pair<Int, Int> = Pair(0, 3)
        val endPosition: Pair<Int, Int> = Pair(9, 8)
        val cellWidth = width.toFloat() / numColumns
        val cellHeight = height.toFloat() / numRows
        val (startCol, startRow) = startPosition
        val (endCol, endRow) = endPosition

        val startX = startCol * cellWidth + 5
        val startY = startRow * cellHeight + 20
        canvas.drawBitmap(start, startX, startY, paint)

        val endX = endCol * cellWidth + 3
        val endY = endRow * cellHeight
        canvas.drawBitmap(end, endX, endY, paint)
    }

    private fun drawEntryCount(canvas: Canvas, position: Pair<Int, Int>, count: Int) {
        val (col, row) = position
        val cellWidth = width.toFloat() / numColumns
        val cellHeight = height.toFloat() / numRows
        val x = col * cellWidth + cellWidth / 2
        val y = row * cellHeight + cellHeight / 6
        paint.color = Color.parseColor("#930000")
        paint.textSize = 30f
        canvas.drawText("$count", x, y, paint)
    }

    private fun drawMonster(canvas: Canvas) {
        monsterIndex.forEach { monsterIndex ->
            val (col, row) = road[monsterIndex]
            val cellWidth = width.toFloat() / numColumns
            val cellHeight = height.toFloat() / numRows
            val x = col * cellWidth
            val y = row * cellHeight + 40
            canvas.drawBitmap(monster, x, y, paint)
        }
    }

    private fun startMovingMonster() {
        val handler = Handler(Looper.getMainLooper())
        val moveRunnable = object : Runnable {
            override fun run() {
                if (!isGameOver) {
                    monsterIndex.forEachIndexed { index, _ ->
                        if (monsterIndex[index] < road.size - 1) {
                            val currentPosition = road[monsterIndex[index]]
                            if (isInUnicornRange(currentPosition)) {
                                decreaseMonsterHealth(index)
                            }
                            monsterIndex[index]++
                            invalidate() // 重新繪製視圖
                        } else {
                            // 怪物到達終點處理
                            endHealth--
                            resetMonster(index)
                            handler.postDelayed({
                                addNewMonster()
                                invalidate()
                            }, 1500)
                                CountWave()
                            if (endHealth <= 0) {
                                stopMonsterMovement()
                                val intent = Intent(context, GameOverActivity::class.java)
                                intent.putExtra("wave", currentWave)
                                stopMonsterMovement()
                                context.startActivity(intent)
                            }
                        }
                    }
                }
                clearMonster()
                handler.postDelayed(this, 500)
            }
        }
        handler.post(moveRunnable)
    }
    private fun decreaseMonsterHealth(index: Int) {
        monsterHealth[index]--
    }

    private fun clearMonster() {
        val deadMonsters = mutableListOf<Int>()
        monsterHealth.forEachIndexed { index, health ->
            if (health <= 0) {
                deadMonsters.add(index)
                money += 5
                resetMonster(index)
                CountWave()
                handler.postDelayed({
                    addNewMonster()
                    invalidate()
                }, 1500)
            }
        }
        deadMonsters.forEach { index ->
            resetMonster(index)
        }
    }

    private fun resetMonster(index: Int) {
        monsterHealth[index] = monsterAllHealth
        monsterIndex[index] = 0
        monsterPosition.clear()
    }

    private fun addNewMonster() {
        monsterIndex.add(0)
        monsterHealth.add(monsterAllHealth)
    }

    private fun CountWave(){
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWaveTime >= 5000) {
            monsterAllHealth++
            currentWave++
            startCountdown(5)
            lastWaveTime = currentTime // 更新上一波的時間
        }
    }

    private fun isInUnicornRange(position: Pair<Int, Int>): Boolean {
        val (col, row) = position
        for (i in -1..1) {
            for (j in -1..1) {
                val towerPosition = Pair(col + i, row + j)
                if (visibleTowers.contains(towerPosition)) {
                    unicornEntryCounts[towerPosition] = unicornEntryCounts.getOrDefault(towerPosition, 20) - 1
                    if (unicornEntryCounts[towerPosition]!! <= 0) {
                        visibleTowers.remove(towerPosition)
                        unicornEntryCounts.remove(towerPosition)
                        showCannonMessage("獨角獸守護者已消失")
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun isTowerVisible(position: Pair<Int, Int>): Boolean {
        return position in visibleTowers
    }

    private fun toggleTowerVisibility(position: Pair<Int, Int>) {
        if (!isTowerVisible(position)) {
            if (money >= 20) {
                visibleTowers.add(position)
                money -= 20
                showCannonMessage("獨角獸守護者已出现")
            } else {
                showMoneyMessage("碎片不足！需要20片")
            }
        } else {
            visibleTowers.remove(position)
            money += 10
            showCannonMessage("獨角獸守護者已離開")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / (width / numColumns)).toInt()
            val row = (event.y / (height / numRows)).toInt()
            val position = Pair(col, row)

            if (position in tower) {
                val clickTime = System.currentTimeMillis()
                if (lastClickedPosition == position && clickTime - lastClickTime < 300) {
                    toggleTowerVisibility(position)
                    invalidate()
                }
                lastClickTime = clickTime
                lastClickedPosition = position
            } else {
                showMoneyMessage("無法在此處召喚獨角獸")
            }
        }
        return true
    }

    private fun drawTower(canvas: Canvas, position: Pair<Int, Int>) {
        if (isTowerVisible(position)) {
            val (col, row) = position
            val cellWidth = width.toFloat() / numColumns
            val cellHeight = height.toFloat() / numRows
            val x = col * cellWidth - 5
            val y = row * cellHeight
            canvas.drawBitmap(cannon, x, y, paint)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val cellWidth: Float = width.toFloat() / numColumns
        val cellHeight: Float = height.toFloat() / numRows

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                if (road.contains(Pair(col, row))) {
                    paint.color = Color.parseColor("#FFE4E1")
                    canvas.drawRect(
                        col * cellWidth, row * cellHeight,
                        (col + 1) * cellWidth, (row + 1) * cellHeight,
                        paint
                    )
                } else if (tower.contains(Pair(col, row))) {
                    paint.color = Color.parseColor("#C1FFC1")
                    canvas.drawRect(
                        col * cellWidth, row * cellHeight,
                        (col + 1) * cellWidth, (row + 1) * cellHeight,
                        paint
                    )
                }else {
                    paint.color = Color.parseColor("#E6E6FA")
                    canvas.drawRect(
                        col * cellWidth, row * cellHeight,
                        (col + 1) * cellWidth, (row + 1) * cellHeight,
                        paint
                    )
                }
            }
        }
    }

    private fun drawUp(canvas: Canvas) {
        val cellWidth: Float = width.toFloat() / numColumns
        val cellHeight: Float = height.toFloat() / numRows
        val up = arrayOf(
            Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(4, 0), Pair(5, 0), Pair(6, 0),
            Pair(7, 0), Pair(8, 0), Pair(9, 0)
        )

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                if (up.contains(Pair(col, row))) {
                    paint.color = Color.WHITE
                    canvas.drawRect(
                        col * cellWidth, 0f,
                        (col + 1) * cellWidth, cellHeight,
                        paint
                    )
                }
            }
        }

        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 5f
        }
        val y = 1 * cellHeight
        canvas.drawLine(0f, y, width.toFloat(), y, paint)
    }

    fun stopMonsterMovement() {
        isGameOver = true
    }
}
