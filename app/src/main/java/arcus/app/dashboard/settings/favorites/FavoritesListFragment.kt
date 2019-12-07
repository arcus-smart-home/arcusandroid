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
package arcus.app.dashboard.settings.favorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import arcus.app.R
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.PreferenceUtils
import arcus.app.common.utils.inflate
import android.widget.Button


class FavoritesListFragment : Fragment() {

    private var mAdapter: FavoritesListAdapter? = null

    val title: String
        get() = getString(R.string.card_favorites_title)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_favorites_list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = title
        val container = activity
        if (container is FragmentContainerHolder) {
            container.showBackButtonOnToolbar(true)
        }

        val mRecyclerView = view.findViewById<View>(R.id.favorites_list) as RecyclerView
        val mRecyclerViewDragDropManager = RecyclerViewDragDropManager()

        activity?.run {
            mAdapter = FavoritesListAdapter(this, FavoritesListDataProvider())
            mRecyclerView.layoutManager = LinearLayoutManager(this)

            mAdapter?.let { mAdapter ->
                mRecyclerView.adapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter)
            }
        }

        mRecyclerView.itemAnimator = RefactoredDefaultItemAnimator()
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView)
    }

    override fun onPause() {
        super.onPause()
        val orderedList = mAdapter?.orderedDeviceIdList ?: return
        PreferenceUtils.putOrderedFavoritesList(orderedList)
    }
}
