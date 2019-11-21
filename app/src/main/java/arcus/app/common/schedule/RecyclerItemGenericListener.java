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
package arcus.app.common.schedule;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.dexafree.materialList.model.CardItemView;


// From http://stackoverflow.com/a/26196831/1610001
public class RecyclerItemGenericListener implements RecyclerView.OnItemTouchListener{

    private RecyclerView mRecyclerView;

    public static interface OnItemClickListener {
        public void onItemClick(CardItemView view, int position, MotionEvent event);
        public void onItemLongClick(CardItemView view, int position);
    }

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    public RecyclerItemGenericListener(Context context, OnItemClickListener listener){
        mListener = listener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e){
                CardItemView childView = (CardItemView)mRecyclerView.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && mListener != null){
                    mListener.onItemLongClick(childView, mRecyclerView.getChildPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e){
        CardItemView childView = (CardItemView)view.findChildViewUnder(e.getX(), e.getY());


        if(childView != null && mListener != null && mGestureDetector.onTouchEvent(e)){
            mListener.onItemClick(childView, view.getChildPosition(childView), e);
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent){}

    public void setRecyclerView(RecyclerView recyclerView){
        mRecyclerView = recyclerView;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }
}
