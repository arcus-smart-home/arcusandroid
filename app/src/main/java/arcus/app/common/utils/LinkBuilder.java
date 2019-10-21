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

import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import arcus.app.ArcusApplication;

import java.lang.ref.WeakReference;



public class LinkBuilder {

    public interface OnLinkClickListener {
        void onLinkClicked(TextView view);
    }

    private final SpannableStringBuilder builder = new SpannableStringBuilder();
    private final TextView view;
    private Integer spanStart;
    private CustomActionSpan spanAction;

    /**
     * Create a LinkBuilder that builds a {@link SpannableStringBuilder} object and sets the given
     * view's text to the value built by this object.
     *
     * @param view The {@link TextView} in which to insert this text.
     */
    public LinkBuilder(TextView view) {
        this.view = view;
    }

    /**
     * Create a LinkBuilder that builds a {@link SpannableStringBuilder} object.
     */
    public LinkBuilder() {
        this.view = null;
    }

    /**
     * Start a clickable span that, when clicked, will launch a browser to the given URI.
     *
     * @param destination URI to show in a brower when span is clicked.
     * @return This LinkBuilder
     */
    public LinkBuilder startLinkSpan(Uri destination) {
        spanStart = builder.length();
        spanAction = new CustomActionSpan(destination);

        return this;
    }

    /**
     * Start a clickable span that, when clicked, will invoke the {@link OnLinkClickListener#onLinkClicked(TextView)}
     * method of the listener.
     *
     * @param listener Listener to invoke when this span is clicked.
     * @return This LinkBuilder
     */
    public LinkBuilder startLinkSpan(OnLinkClickListener listener) {
        spanStart = builder.length();
        spanAction = new CustomActionSpan(new WeakReference<>(listener));

        return this;
    }

    /**
     * Ends the current span.
     *
     * @throws IllegalStateException if no span is in progress
     * @return This LinkBuilder
     */
    public LinkBuilder endLinkSpan() {
        if (spanAction == null || spanStart == null) {
            throw new IllegalStateException("endLinkSpan() invoked without preceding startLinkSpan().");
        }

        builder.setSpan(spanAction, spanStart, builder.length(), 0);
        spanAction = null;
        spanStart = null;

        return this;
    }

    /**
     * Appends the given {@link CharSequence} to this LinkBuilder.
     * @param text Text to append
     * @return This LinkBuilder
     */
    public LinkBuilder appendText(CharSequence text) {
        builder.append(text);
        return this;
    }

    /**
     * Appends the text specified by the given String resource id.
     * @param stringResId The String to append
     * @return This LinkBuilder
     */
    public LinkBuilder appendText(int stringResId) {
        builder.append(ArcusApplication.getContext().getString(stringResId));
        return this;
    }

    /**
     * Returns a {@link SpannableStringBuilder}. When the constructor was called with a non-null
     * view, also sets the text of that view to this link and applies {@link TextView#setMovementMethod(MovementMethod)}.
     * @return SpannableStringBuilder representing this link.
     */
    public SpannableStringBuilder build() {
        if (view != null) {
            view.setText(builder);
            view.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return builder;
    }

    private class CustomActionSpan extends ClickableSpan {

        private WeakReference<OnLinkClickListener> listenerWeakReference;
        private Uri link;

        CustomActionSpan(Uri link) {
            this.link = link;
        }

        CustomActionSpan(WeakReference<OnLinkClickListener> listener) {
            this.listenerWeakReference = listener;
        }

        @Override
        public void onClick(View view) {
            if (link != null) {
                ActivityUtils.launchUrl(link);
            } else if (listenerWeakReference != null && listenerWeakReference.get() != null) {
                listenerWeakReference.get().onLinkClicked((TextView) view);
            }
        }
    }

}
