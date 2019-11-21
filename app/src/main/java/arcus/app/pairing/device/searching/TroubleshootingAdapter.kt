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
package arcus.app.pairing.device.searching

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.factoryreset.FactoryResetDeviceActivity
import arcus.presentation.pairing.device.searching.HelpStep
import arcus.presentation.pairing.device.searching.HelpStepType


class TroubleshootingStepsAdapter(val activity: Activity, private val helpSteps: List<HelpStep>) : RecyclerView.Adapter<TroubleshootingStepsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TroubleshootingStepsViewHolder {
        return TroubleshootingStepsViewHolder(activity, parent.inflate(R.layout.list_item_pairing_help_step))
    }

    override fun getItemCount() = helpSteps.size

    override fun onBindViewHolder(holder: TroubleshootingStepsViewHolder, position: Int) {
        holder.bindView(helpSteps[position], position)
    }
}

class TroubleshootingStepsViewHolder(val activity: Activity, view: View) : RecyclerView.ViewHolder(view) {
    private val stepImage = itemView.findViewById<AppCompatImageView>(R.id.step_number)
    private val stepContent = itemView.findViewById<ScleraTextView>(R.id.step_content)

    fun bindView(step: HelpStep, position: Int) {
        stepImage.setImageResource(getIconFor(position))
        when (step.action) {
            HelpStepType.INFO, HelpStepType.PAIRING_STEPS -> {
                stepContent.text = step.message
            }

            HelpStepType.FACTORY_RESET -> {
                stepContent.text = step.message
                stepContent.paintFlags = stepContent.paintFlags or
                        Paint.UNDERLINE_TEXT_FLAG
                itemView.setOnClickListener {
                    ContextCompat.startActivity(
                        activity,
                        Intent(activity, FactoryResetDeviceActivity::class.java),
                        null
                    )
                }
            }

            HelpStepType.LINK -> {
                stepContent.text = step.link?.text
                stepContent.paintFlags = stepContent.paintFlags or
                        Paint.UNDERLINE_TEXT_FLAG
                itemView.setOnClickListener {
                    ActivityUtils.launchUrl(Uri.parse(step.link?.url))
                }
            }
            HelpStepType.FORM -> {
                stepContent.text = step.message
            }
        }
    }

    @DrawableRes
    private fun getIconFor(position: Int): Int {
        return when (position) {
            0 -> R.drawable.step1
            1 -> R.drawable.step2
            2 -> R.drawable.step3
            3 -> R.drawable.step4
            4 -> R.drawable.step5
            5 -> R.drawable.step6
            else -> R.drawable.icon_alert
        }
    }
}
