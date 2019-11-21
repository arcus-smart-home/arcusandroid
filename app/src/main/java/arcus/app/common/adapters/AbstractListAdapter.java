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
package arcus.app.common.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractListAdapter<V, K extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<K> {

    @NonNull
    protected List<V> mData = new ArrayList<V>();

    @Override
    public abstract K onCreateViewHolder(ViewGroup viewGroup, int i);

    @Override
    public abstract void onBindViewHolder(K k, int i);

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public V getItem (int position) {
        return mData.get(position);
    }

    public void setData(@NonNull final List<V> data) {
        if (dataSetDiffers(data)) {
            mData = new ArrayList<>(data);
            notifyDataSetChanged();
        }
    }

    private boolean dataSetDiffers (List<V> data) {
        if (data.size() != mData.size()) {
            return true;
        }

        for (int index = 0; index < data.size(); index++) {
            if (!data.get(index).equals(mData.get(index))) {
                return true;
            }
        }

        return false;
    }
}
