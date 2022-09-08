package com.example.todoperfect.ui.add

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.core.view.marginStart
import com.example.todoperfect.R

class DayOfWeekPicker(context: Context, attrs: AttributeSet) : RadioGroup(context, attrs) {

    private var itemClickListener: ItemClickListener? = null
    private val radioButtonList = ArrayList<RadioButton>()
    private val dayOfWeekList = mapOf(
        0 to "S", 1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S"
    )
//
    init {
        orientation = HORIZONTAL
        for (i in 0..6) {
            add(i)
        }
        setOnCheckedChangeListener { group, checkedId ->
            val radio = group.findViewById<RadioButton>(checkedId)
            if (!radio.isChecked) return@setOnCheckedChangeListener
            val dayOfWeek: Int = radio.tag as Int
            itemClickListener?.onClick(dayOfWeek, indexOfChild(radio))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(dpToPixel(350), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(dpToPixel(35), MeasureSpec.EXACTLY))
    }

    @SuppressLint("ResourceType", "UseCompatLoadingForColorStateLists",
        "UseCompatLoadingForDrawables"
    )
    private fun add(dayOfWeek: Int) {
        val radioButton = RadioButton(context).apply {
            text = dayOfWeekList[dayOfWeek]
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(resources.getColorStateList(R.drawable.selector_dayofweek_text))
            background = resources.getDrawable(R.drawable.selector_dayofweek_background)
            val params = LayoutParams(dpToPixel(35), dpToPixel(35), 1F)
            params.marginStart = dpToPixel(2)
            params.marginEnd = dpToPixel(2)
            layoutParams = params
            gravity = Gravity.CENTER
            tag = dayOfWeek
            buttonDrawable = null
        }
        this.addView(radioButton)
        radioButtonList.add(radioButton)
    }

    private fun dpToPixel(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), resources.displayMetrics).toInt()
    }

    fun checkDay(dayOfWeek: Int) {
        radioButtonList[dayOfWeek].isChecked = true
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onClick(dayOfWeek: Int, position: Int)
    }

}