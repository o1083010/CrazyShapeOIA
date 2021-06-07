package tw.edu.pu.csim.tcyang.crazyshape

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.support.image.TensorImage
import tw.edu.pu.csim.tcyang.crazyshape.ml.Shapes
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GameActivity : AppCompatActivity() {

    var FlagShape: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        var intent= getIntent()
        FlagShape = intent.getIntExtra("形狀",0)
        when (FlagShape) {
            1 -> txvMsg.text = "請畫出圓形"
            2 -> txvMsg.text = "請畫出方形"
            3 -> txvMsg.text = "請畫出星型"
            4 -> txvMsg.text = "請畫出三角型"
        }

        btnBack.isEnabled = false

        btnBack.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                finish()
            }
        })

        btn.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                handv.path.reset()
                handv.invalidate()
            }
        })

        /*btnSave.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                SaveToStorage(bitmap)
            }
        })*/

        handv.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                var xPos = event.getX()
                var yPos = event.getY()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> handv.path.moveTo(xPos, yPos)
                    MotionEvent.ACTION_MOVE -> handv.path.lineTo(xPos, yPos)
                    MotionEvent.ACTION_UP -> {
                        //將handv轉成Bitmap
                        val b = Bitmap.createBitmap(handv.measuredWidth, handv.measuredHeight,
                                Bitmap.Config.ARGB_8888)
                        val c = Canvas(b)
                        handv.draw(c)
                        classifyDrawing(b)
                        SaveToStorage(b)
                    }
                }
                handv.invalidate()
                return true
            }
        })
    }

    fun SaveToStorage(bmp:Bitmap){
        //將圖片換成byteArray
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val pictData = baos.toByteArray()

        //val filename = "images/pict.jpg"  //設定子節點與檔名

        //根據系統時間設定檔名
        val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")  //定義時間格式
        val dt = Date()  //取得現在時間
        val dts = sdf.format(dt) //將目前日期轉為字串
        val filename = "images/" + dts + ".jpg"  //設定子節點與檔名

        val reference = FirebaseStorage.getInstance().getReference().child(filename)
        //上傳到Firebase
        reference.putBytes(pictData)
                .addOnSuccessListener {
                    Toast.makeText(baseContext, "上傳成功", Toast.LENGTH_SHORT).show()
                }

                .addOnFailureListener {
                    Toast.makeText(baseContext, "上傳失敗", Toast.LENGTH_SHORT).show()
                }
    }
    fun classifyDrawing(bitmap : Bitmap) {
        val model = Shapes.newInstance(this)

        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

        // Runs model inference and gets result.
        //val outputs = model.process(image)
        //val probability = outputs.probabilityAsCategoryList

        val outputs = model.process(image)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // 排序，高匹配率優先
                }.take(1)  //取最高的1個
        var Result:String = ""
        var FlagDraw:Int = 0
        when (outputs[0].label) {
            "circle" -> {Result = "圓形"
                FlagDraw=1}
            "square" -> {Result = "方形"
                FlagDraw=2}
            "star" -> {Result = "星形"
                FlagDraw=3}
            "triangle" -> {Result = "三角形"
                FlagDraw=4}
        }
        //Result += ": " + String.format("%.1f%%", outputs[0].score * 100.0f)
        Result = "你畫的是" + Result + ","
        if (FlagShape==FlagDraw){
            Result += "恭喜順利過關!"
            btnBack.isEnabled = true
        }
        else{
            Result += "請再試試看喔！"
        }


        // Releases model resources if no longer used.
        model.close()
        Toast.makeText(this, Result, Toast.LENGTH_SHORT).show()
    }
}