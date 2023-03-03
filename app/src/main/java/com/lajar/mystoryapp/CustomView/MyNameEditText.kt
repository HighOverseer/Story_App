package com.lajar.mystoryapp.CustomView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputEditText
import com.lajar.mystoryapp.R

class MyNameEditText : TextInputEditText {
    private lateinit var errorDrawable: Drawable
    private val mPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        errorDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_24) as Drawable

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    showError()
                } else {
                    hideError()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (compoundDrawables[0] != null) {
            mPaint.apply {
                textSize = 11F.toSp()
                color = ResourcesCompat.getColor(resources, R.color.red_700, null)
                typeface = ResourcesCompat.getFont(context, R.font.poppins_regular)
            }
            val x = 46F.toDp()
            val y = 52F.toDp()
            canvas?.drawText(context.resources.getString(R.string.name_blank), x, y, mPaint)
        }
    }

    private fun showError() {
        setEditTextDrawable(startOfTheText = errorDrawable)
    }

    private fun hideError() {
        setEditTextDrawable()
    }


    private fun setEditTextDrawable(
        startOfTheText: Drawable? = null,
        TopOfTheText: Drawable? = null,
        EndOfTheText: Drawable? = null,
        BottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            TopOfTheText,
            EndOfTheText,
            BottomOfTheText
        )
    }

    private fun Float.toDp():Float{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
    }

    private fun Float.toSp():Float{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)
    }


}