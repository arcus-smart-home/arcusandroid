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

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import arcus.app.R;

public class SpinnableImageView extends ImageView {

    private SpinDirection direction = SpinDirection.CLOCKWISE;
    private boolean isSpinning = false;
    private int spinDurationMs = 1000;

    private Animation rotation = getAnimationForDirection(direction);

    public SpinnableImageView(Context context) {
        super(context);
    }

    public SpinnableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpinnableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SpinnableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSpinning (boolean enabled) {
        this.isSpinning = enabled;
        updateAnimation();
    }

    public boolean isSpinning () {
        return this.isSpinning;
    }

    public void setSpinDirection (SpinDirection direction) {
        this.direction = direction;
        updateAnimation();
    }

    public enum SpinDirection {
        CLOCKWISE, COUNTER_CLOCKWISE
    }

    public void setSpinDuration (int durationMs) {
        this.spinDurationMs = durationMs;
        updateAnimation();
    }

    public int getSpinDuration () {
        return this.spinDurationMs;
    }

    private Animation getAnimationForDirection (SpinDirection direction) {
        Animation rotation = AnimationUtils.loadAnimation(getContext(), direction == SpinDirection.CLOCKWISE ? R.anim.clockwise_rotation : R.anim.counterclockwise_rotation);
        rotation.setDuration(spinDurationMs);
        rotation.setRepeatCount(Animation.INFINITE);

        return rotation;
    }

    private void updateAnimation () {
        if (isSpinning) {
            this.startAnimation(getAnimationForDirection(direction));
        } else {
            this.clearAnimation();
        }

    }
}
