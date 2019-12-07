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
@file:JvmMultifileClass
package arcus.app.subsystems.debug

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import arcus.app.R
import arcus.app.common.utils.inflate
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import kotlin.properties.Delegates

abstract class DebugMenuOption {
    abstract fun getItemType() : Int

    companion object {
        const val BINARY_OPTION = 1
        const val TEXT_OPTION = 2
        const val HEADER_OPTION = 3
        const val BUTTON_OPTION = 4
    }
}

data class BinaryOption(
        var switchText: String,
        var descriptionText: String,
        var isChecked: Boolean,
        var detailsText: String? = null,
        val checkChangedListener: (BinaryOption, Boolean) -> Boolean = { _, _ -> false }
) : DebugMenuOption() {
    override fun getItemType() = BINARY_OPTION
}

data class TextOption(
        val titleText: String,
        val descriptionText: String
) : DebugMenuOption() {
    override fun getItemType() = TEXT_OPTION
}

data class HeaderOption(
        val titleText: String
) : DebugMenuOption() {
    override fun getItemType() = HEADER_OPTION
}

data class ButtonOption(
        var buttonText: String,
        var titleText: String,
        var descriptionText: String,
        var detailsText: String? = null,
        val clickListener: (ButtonOption, RecyclerView.Adapter<*>) -> Boolean = { _, _ -> false }
) : DebugMenuOption() {
    override fun getItemType() = BUTTON_OPTION
}



abstract class BindingViewHolder<in T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: T)

    open fun bindListener(adapter: RecyclerView.Adapter<*>, data: T) {
        // No - Op
    }
}

class HeaderOptionViewHolder(view: View) : BindingViewHolder<HeaderOption>(view) {
    private var title by Delegates.notNull<ScleraTextView>()

    override fun bind(data: HeaderOption) {
        title.text = data.titleText
    }

    companion object {
        @JvmStatic
        fun inflateUsing(viewGroup: ViewGroup) : HeaderOptionViewHolder {
            val itemView = viewGroup.inflate(R.layout.debug_menu_header_view)
            val viewHolder = HeaderOptionViewHolder(itemView)

            viewHolder.title = itemView as ScleraTextView

            return viewHolder
        }
    }
}

class BinaryOptionViewHolder(view: View) : BindingViewHolder<BinaryOption>(view) {
    private var switch by Delegates.notNull<Switch>()
    private var description by Delegates.notNull<ScleraTextView>()
    private var details by Delegates.notNull<ScleraTextView>()

    override fun bind(data: BinaryOption) {
        switch.text = data.switchText
        switch.isChecked = data.isChecked
        description.text = data.descriptionText
        if (data.detailsText.isNullOrEmpty()) {
            details.visibility = View.GONE
        } else {
            details.visibility = View.VISIBLE
            details.text = data.detailsText
        }
    }

    override fun bindListener(adapter: RecyclerView.Adapter<*>, data: BinaryOption) {
        switch.setOnClickListener { _ ->
            if (data.checkChangedListener(data, switch.isChecked)) {
                adapter.notifyItemChanged(adapterPosition)
            }
        }
    }

    companion object {
        @JvmStatic
        fun inflateUsing(viewGroup: ViewGroup) : BinaryOptionViewHolder {
            val itemView = viewGroup.inflate(R.layout.debug_menu_binary_card)
            val viewHolder = BinaryOptionViewHolder(itemView)

            viewHolder.switch = itemView.findViewById(R.id.debug_menu_binary_switch)
            viewHolder.description = itemView.findViewById(R.id.debug_menu_description_text_view)
            viewHolder.details = itemView.findViewById(R.id.debug_menu_details_text_view)

            return viewHolder
        }
    }
}

class TextOptionViewHolder(view: View) : BindingViewHolder<TextOption>(view) {
    private var title by Delegates.notNull<ScleraTextView>()
    private var description by Delegates.notNull<ScleraTextView>()

    override fun bind(data: TextOption) {
        title.text = data.titleText
        description.text = data.descriptionText
    }

    companion object {
        @JvmStatic
        fun inflateUsing(viewGroup: ViewGroup) : TextOptionViewHolder {
            val itemView = viewGroup.inflate(R.layout.debug_menu_text_card)
            val viewHolder = TextOptionViewHolder(itemView)

            viewHolder.title = itemView.findViewById(R.id.debug_menu_title_text_view)
            viewHolder.description = itemView.findViewById(R.id.debug_menu_description_text_view)

            return viewHolder
        }
    }
}

class ButtonOptionViewHolder(view: View) : BindingViewHolder<ButtonOption>(view) {
    private var button by Delegates.notNull<Button>()
    private var title by Delegates.notNull<ScleraTextView>()
    private var description by Delegates.notNull<ScleraTextView>()
    private var details by Delegates.notNull<ScleraTextView>()

    override fun bind(data: ButtonOption) {
        button.text = data.buttonText
        title.text = data.titleText
        description.text = data.descriptionText
        if (data.detailsText.isNullOrEmpty()) {
            details.visibility = View.GONE
        } else {
            details.visibility = View.VISIBLE
            details.text = data.detailsText
        }
    }

    override fun bindListener(adapter: RecyclerView.Adapter<*>, data: ButtonOption) {
        button.setOnClickListener {
            data.clickListener(data, adapter)
        }
    }

    companion object {
        @JvmStatic
        fun inflateUsing(viewGroup: ViewGroup) : ButtonOptionViewHolder {
            val itemView = viewGroup.inflate(R.layout.debug_menu_button_card)
            val viewHolder = ButtonOptionViewHolder(itemView)

            viewHolder.button = itemView.findViewById(R.id.debug_menu_action_button)
            viewHolder.title = itemView.findViewById(R.id.debug_menu_title_text_view)
            viewHolder.description = itemView.findViewById(R.id.debug_menu_description_text_view)
            viewHolder.details = itemView.findViewById(R.id.debug_menu_details_text_view)

            return viewHolder
        }
    }
}




class DebugMenuAdapter(
        val items: List<DebugMenuOption>
) : RecyclerView.Adapter<BindingViewHolder<DebugMenuOption>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<DebugMenuOption> {
        @Suppress("UNCHECKED_CAST")
        return when (viewType) {
            DebugMenuOption.BINARY_OPTION -> BinaryOptionViewHolder.inflateUsing(parent)
            DebugMenuOption.TEXT_OPTION -> TextOptionViewHolder.inflateUsing(parent)
            DebugMenuOption.HEADER_OPTION -> HeaderOptionViewHolder.inflateUsing(parent)
            DebugMenuOption.BUTTON_OPTION -> ButtonOptionViewHolder.inflateUsing(parent)
            else -> throw RuntimeException("Not sure how to handle view type of $viewType")
        } as BindingViewHolder<DebugMenuOption>
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].getItemType()

    override fun onBindViewHolder(holder: BindingViewHolder<DebugMenuOption>, position: Int) {
        holder.bind(items[position])
        holder.bindListener(this, items[position])
    }
}
