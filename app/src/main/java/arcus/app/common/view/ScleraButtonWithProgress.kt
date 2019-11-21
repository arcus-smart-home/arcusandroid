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
import android.widget.ProgressBar

import arcus.app.R

class ScleraButtonWithProgress : ConstraintLayout {
    private var buttonText: CharSequence? = null
    private lateinit var initialButtonColor: ScleraButtonColor

    lateinit var scleraButton: ScleraButton
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

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ScleraButtonWithProgress)

        buttonText = attributes.getText(R.styleable.ScleraButtonWithProgress_buttonText)

        val buttonColor = attributes.getInteger(R.styleable.ScleraButtonWithProgress_scleraButtonColor, 0)
        initialButtonColor = ScleraButtonColor.values()[buttonColor]

        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        scleraButton = getChildAt(0) as ScleraButton
        progressBar  = getChildAt(1) as ProgressBar

        scleraButton.text = buttonText
        scleraButton.setColorScheme(initialButtonColor)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            buttonText = scleraButton.text
            scleraButton.text = null
            progressBar.visibility = View.VISIBLE
        } else {
            scleraButton.text = buttonText
            buttonText = null
            progressBar.visibility = View.GONE
        }

        scleraButton.isEnabled = enabled
    }

    override fun setOnClickListener(listener: View.OnClickListener?) {
        scleraButton.setOnClickListener(listener)
    }
}
