package tw.edu.pu.csim.tcyang.crazyshape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class HandPaint(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)  //畫筆(避免踞齒)
    var path : Path = Path() //繪圖軌跡

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE //描邊
        paint.strokeWidth = 60f  //設置畫筆寬度
        paint.strokeCap = Paint.Cap.ROUND //線帽(線條開始區域，筆觸端點)平滑
        paint.strokeJoin = Paint.Join.ROUND //連接處圓弧
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)  //背景
        canvas.drawPath(path, paint)
    }

}