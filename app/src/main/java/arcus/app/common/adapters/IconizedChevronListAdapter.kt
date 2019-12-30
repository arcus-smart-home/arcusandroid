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
package arcus.app.common.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.models.ListItemModel
import java.util.ArrayList

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
open class IconizedChevronListAdapter : ArrayAdapter<ListItemModel?> {
    var isUseLightColorScheme = true

    constructor(context: Context?) : super(context, 0)
    constructor(context: Context?, data: ArrayList<ListItemModel>) : super(context, 0) {
        super.addAll(data)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listdata_item, parent, false)
        }
        val mListData = getItem(position)
        val chevronImage =
            convertView!!.findViewById<ImageView>(R.id.imageChevron)
        val imageIcon = convertView.findViewById<ImageView>(R.id.imageIcon)
        val topText = convertView.findViewById<TextView>(R.id.tvTopText)
        val bottomText = convertView.findViewById<TextView>(R.id.tvBottomText)
        chevronImage.setImageResource(R.drawable.chevron)
        topText.text = mListData!!.text
        if (mListData.subText == null || mListData.subText.length < 1) {
            bottomText.visibility = View.GONE
        } else {
            bottomText.visibility = View.VISIBLE
            bottomText.text = mListData.subText
        }
        if (imageIcon != null && mListData.imageResId != null) {
            imageIcon.visibility = View.VISIBLE
            var transformation = BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK)
            if (isUseLightColorScheme) {
                transformation = BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE)
            }
            ImageManager.with(context)
                .putDrawableResource(mListData.imageResId)
                .withTransformForStockImages(transformation)
                .into(imageIcon)
                .execute()
        }
        return convertView
    }

}
