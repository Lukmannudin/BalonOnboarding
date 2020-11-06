package com.oleg.balononboarding

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.oleg.balononboarding.showcaseview.GuideView
import com.oleg.balononboarding.showcaseview.config.AlignType
import com.oleg.balononboarding.showcaseview.config.DismissType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvHello = findViewById<TextView>(R.id.hello)

        GuideView.Builder(this)
            .setTitle("Title")
            .setContentText("Welcome abroad to BalonOnboarding")
            .setDismissType(DismissType.button)
            .setViewAlign(AlignType.center)
            .setTitleGravity(Gravity.START)
            .setContentGravity(Gravity.START)
            .setButtonGravity(Gravity.CENTER)
            .setButtonGravity(Gravity.END)
            .setPaddingTitle(14, 16, 14, 8)
            .setPaddingMessage(14, 0, 14, 8)
            .setButtonText("OK")
            .setTitleTypeFace(ResourcesCompat.getFont(this, R.font.montserrat_bold))
            .setContentTypeFace(ResourcesCompat.getFont(this, R.font.montserrat_regular))
            .setTargetView(tvHello)
            .build()
            .show()
    }
}