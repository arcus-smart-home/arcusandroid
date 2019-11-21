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
package arcus.app.common.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DashboardRecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder view, int position);
        void onDragRight(RecyclerView.ViewHolder view, int position);
        void onDragLeft(RecyclerView.ViewHolder view, int position);
    }

    GestureDetector mGestureDetector;
    int startDragX = 0;
    int startDragY = 0;
    RecyclerView.ViewHolder startDragItem;

    public DashboardRecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null) {
            if(mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(view.getChildViewHolder(childView), view.getChildAdapterPosition(childView));
            }
            else if(e.getAction() == MotionEvent.ACTION_DOWN) {
                startDragX = (int) e.getRawX();
                startDragY = (int) e.getRawY();
                startDragItem = view.getChildViewHolder(childView);
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
                int xCoord = (int) e.getRawX();
                int yCoord = (int) e.getRawY();
                RecyclerView.ViewHolder selectedView = view.getChildViewHolder(childView);
                if(Math.abs(startDragY-yCoord) > Math.abs(startDragX - xCoord)) {
                    return false;
                }
                if(xCoord - startDragX > 10) {
                    Log.d("**Russ", "flipright");
                    mListener.onDragRight(startDragItem, view.getChildAdapterPosition(childView));
                } else if(startDragX - xCoord > 10) {
                    Log.d("**Russ", "flipleft");
                    mListener.onDragLeft(startDragItem, view.getChildAdapterPosition(childView));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
