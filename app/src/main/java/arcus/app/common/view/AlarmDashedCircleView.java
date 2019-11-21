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
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import android.util.AttributeSet;


public class AlarmDashedCircleView extends DashedCircleView {


    public AlarmDashedCircleView(@NonNull Context context) {
        super(context);
    }

    public AlarmDashedCircleView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlarmDashedCircleView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlarmDashedCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /*@Override
    protected void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        mRedPaint = new Paint();
        mRedPaint.setAntiAlias(true);
        mRedPaint.setColor(Color.WHITE);
        mRedPaint.setStyle(Paint.Style.STROKE);
        mRedPaint.setStrokeWidth(mCircleWidth);
        mRedPaint.setShadowLayer(mShadowLayerStroke, 0, 0, Color.WHITE);

    }*/

    /*@Override
    protected void drawPartial(@NonNull Canvas canvas){
        if(mDashes ==1){
            drawOneDevice(canvas);
            return;
        }

        final float sweep = CIRCLE/mDashes - mGap;
        int count =0;

        //draw offline dashes
        for(int i=0;i< mOffline;i++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mGreyPaint);
            count ++;
        }

        //draw bypassed dashes
        for(int j=0;j<mBypassed;j++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mGreyPaint);
            count ++;
        }

        //draw active dashes
        for(int k =0; k<mActive;k++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mWhiteGlowPaint);
            count++;
        }
    }*/

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        switch (alarmState){
            case ALERT:
                cleanCanvas(canvas);
                drawCircle(canvas, mWhiteGlowPaint);
                break;
            default:
                super.onDraw(canvas);
        }
    }
}
