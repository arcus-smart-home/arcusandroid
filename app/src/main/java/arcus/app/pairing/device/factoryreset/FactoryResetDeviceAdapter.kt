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
package arcus.app.pairing.device.factoryreset

import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.factoryreset.FactoryResetStep

class FactoryResetDeviceAdapter(private val resetSteps: ArrayList<FactoryResetStep>) : RecyclerView.Adapter<FactoryResetStepsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactoryResetStepsViewHolder {
        return FactoryResetStepsViewHolder(parent.inflate(R.layout.list_factory_reset_step_item))
    }

    override fun getItemCount() = resetSteps.size

    override fun onBindViewHolder(holder: FactoryResetStepsViewHolder, position: Int) {
        holder?.bindView(resetSteps[position], position)
    }
}

class FactoryResetStepsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val stepImage = itemView.findViewById<AppCompatImageView>(R.id.factory_reset_step_number)
    private val stepContent = itemView.findViewById<ScleraTextView>(R.id.factory_reset_step_content)

    fun bindView(resetStep: FactoryResetStep, position: Int) {
        stepImage.setImageResource(getIconFor(position))
        var instructions = resetStep.instructions.toString()
        instructions = instructions.replace("[", "")
        instructions = instructions.replace("]", "")
        stepContent.text = instructions
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
