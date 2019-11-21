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
package arcus.app.common.utils;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.TouchDelegate;
import android.view.View;

import java.util.Map;


public class ViewUtils {

    public static String getQuestionKey(String value, @NonNull Map<String, String> questionMap) {
        for (Map.Entry<String, String> entry : questionMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return "";
    }

    @NonNull
    public static SpannableString appendSmallTextToHint(@NonNull String original, String append) {
        String span = original + " " + append;
        SpannableString ss = new SpannableString(span);
        ss.setSpan(new RelativeSizeSpan(0.5f), original.length() + 1, span.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return ss;
    }

    public static void removeUnderlines(@NonNull Spannable span) {
        for (URLSpan u: span.getSpans(0, span.length(), URLSpan.class)) {
            span.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, span.getSpanStart(u), span.getSpanEnd(u), 0);
        }
    }

    public static void removeUnderlines(@NonNull Spannable... spans) {
        for (Spannable span : spans) {
            removeUnderlines(span);
        }
    }

    /**
     * Adds a touch delegate to the views parent if the parent is of type View.
     * This method is posted back to the parent so that the hit area is applied *after* the parent lays out its children
     *
     * This is useful if you need to expand the hit area (eg: for onClick's to trigger) but don't want to, or can't, increase
     * the size/padding of the view.
     *
     * @param toThisView the View you want to increase the touch (hit) area for
     * @param leftPX the additional amount (negative increases, positive decreases) of the hit area
     * @param topPX the additional amount (negative increases, positive decreases) of the hit area
     * @param rightPX the additional amount (positive increases, negative decreases) of the hit area
     * @param bottomPX the additional amount (positive increases, negative decreases) of the hit area
     */
    public static void increaseTouchArea(
          final View toThisView,
          final int leftPX,
          final int topPX,
          final int rightPX,
          final int bottomPX
    ) {
        if (toThisView == null || !View.class.isInstance(toThisView.getParent())) {
            return;
        }

        final View statementParent = (View) toThisView.getParent();
        statementParent.post(new Runnable() {
            @Override public void run() {
                Rect delegateArea = new Rect();
                toThisView.getHitRect(delegateArea);
                delegateArea.left += leftPX;
                delegateArea.top += topPX;
                delegateArea.right += rightPX;
                delegateArea.bottom += bottomPX;
                statementParent.setTouchDelegate(new TouchDelegate(delegateArea, toThisView));
            }
        });
    }
}
