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

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import arcus.app.R;


public class DashboardFlipViewHolder extends RecyclerView.ViewHolder {

    boolean isBackVisible = false;
    AnimatorSet setRightOut = null;
    AnimatorSet setLeftIn = null;
    AnimatorSet setRightIn = null;
    AnimatorSet setLeftOut = null;
    FrameLayout front;
    FrameLayout back;
    Context context;
    int previousFlipDirection;
    private final int NOT_SET = -1, LEFT = 0, RIGHT = 1;

    public DashboardFlipViewHolder(View view) {
        super(view);
        context = view.getContext();
        previousFlipDirection = NOT_SET;
        setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.card_flip_right_out);

        setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.card_flip_left_in);

        setRightIn = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.card_flip_right_in);

        setLeftOut = (AnimatorSet) AnimatorInflater.loadAnimator(context,
                R.animator.card_flip_left_out);
        front = (FrameLayout) view.findViewById(R.id.card_front);
        back = (FrameLayout) view.findViewById(R.id.card_back);
    }

    public void flipCardLeft() {
        if(previousFlipDirection != LEFT) {
            isBackVisible = !isBackVisible;
            back.setVisibility(View.VISIBLE);
        }
        if(!isBackVisible){
            setRightOut.setTarget(front);
            setRightIn.setTarget(back);
            setRightOut.start();
            setRightIn.start();
            isBackVisible = true;
        }
        else{
            setRightOut.setTarget(back);
            setRightIn.setTarget(front);
            setRightOut.start();
            setRightIn.start();
            isBackVisible = false;
        }
        previousFlipDirection = LEFT;
    }

    public void flipCardRight() {
        if(previousFlipDirection != RIGHT) {
            isBackVisible = !isBackVisible;
            back.setVisibility(View.VISIBLE);
        }
        if(!isBackVisible){
            setLeftIn.setTarget(front);
            setLeftOut.setTarget(back);
            setLeftIn.start();
            setLeftOut.start();
            isBackVisible = true;
        }
        else{
            setLeftIn.setTarget(back);
            setLeftOut.setTarget(front);
            setLeftIn.start();
            setLeftOut.start();
            isBackVisible = false;
        }
        previousFlipDirection = RIGHT;
    }
}
