package arcus.presentation.rules.list

import android.os.Parcelable
import arcus.presentation.common.view.ContentsComparable
import kotlinx.android.parcel.Parcelize

/**
 * Represents a Rule item to be shown.
 */
sealed class ListItem : Parcelable, ContentsComparable<ListItem> {
    /**
     * The display-able title of the rule.
     */
    abstract val title: String

    @Parcelize
    data class Header(
        override val title: String,
        val ruleCount: Int
    ) : ListItem(), ContentsComparable<ListItem> {
        override fun areItemsTheSame(newItem: ListItem): Boolean =
                title == newItem.title

        override fun areContentsTheSame(newItem: ListItem): Boolean = newItem is Header &&
                ruleCount == newItem.ruleCount &&
                title == newItem.title
    }

    /**
     * A rule list item.
     *
     * @property subtitle The sub text for this item.
     * @property hasSchedule If the rule has a schedule associated with it or not.
     * @property ruleAddress The address of the rule.
     * @property templateId The ID of the rules template.
     * @property isEnabled If the rule is enabled or not.
     */
    @Parcelize
    data class Rule(
        override val title: String,
        val subtitle: String,
        val hasSchedule: Boolean,
        val ruleAddress: String,
        val templateId: String,
        var isEnabled: Boolean
    ) : ListItem(), ContentsComparable<ListItem> {
        override fun areItemsTheSame(newItem: ListItem): Boolean =
                newItem is Rule && ruleAddress == newItem.ruleAddress

        override fun areContentsTheSame(newItem: ListItem): Boolean = newItem is Rule &&
                title == newItem.title &&
                subtitle == newItem.subtitle &&
                isEnabled == newItem.isEnabled &&
                hasSchedule == newItem.hasSchedule
    }
}
