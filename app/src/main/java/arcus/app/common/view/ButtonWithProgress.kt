/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.common.view

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ProgressBar

import arcus.app.R

class ButtonWithProgress : ConstraintLayout {
    private var buttonText: CharSequence? = null

    lateinit var button: Button
        private set
    lateinit var progressBar: ProgressBar
        private set

    constructor(
        context: Context
    ) : super(context) {
        initView(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.sclera_button_with_progress, this)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ButtonWithProgress)

        buttonText = attributes.getText(R.styleable.ButtonWithProgress_buttonText)
        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        button = getChildAt(0) as Button
        progressBar  = getChildAt(1) as ProgressBar

        button.text = buttonText
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            buttonText = button.text
            button.text = null
            progressBar.visibility = View.VISIBLE
        } else {
            button.text = buttonText
            buttonText = null
            progressBar.visibility = View.GONE
        }

        button.isEnabled = enabled
    }

    override fun setOnClickListener(listener: View.OnClickListener?) {
        button.setOnClickListener(listener)
    }
}
