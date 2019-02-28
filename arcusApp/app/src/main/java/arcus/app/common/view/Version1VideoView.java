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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

public class Version1VideoView extends VideoView {
    public interface OnSeekStartListener {
        void seekStarted();
    }

    private OnSeekStartListener onSeekStartListener;
    private static final int SHOW_SEEKING = 100;
    @NonNull
    private IncomingHandler videoHandler = new IncomingHandler(this);

    public Version1VideoView(Context context) {
        super(context);
    }

    public Version1VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Version1VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public Version1VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void seekTo(int msec) {
        super.seekTo(msec);

        videoHandler.removeMessages(SHOW_SEEKING);

        Message message = videoHandler.obtainMessage(SHOW_SEEKING);
        videoHandler.sendMessageDelayed(message, 500); // 500 MS.
    }

    public OnSeekStartListener getOnSeekStartListener() {
        return onSeekStartListener;
    }

    public void setOnSeekStartListener(OnSeekStartListener onSeekStartListener) {
        this.onSeekStartListener = onSeekStartListener;
    }

    private static class IncomingHandler extends Handler {
        private WeakReference<Version1VideoView> videoViewRef;

        public IncomingHandler(Version1VideoView videoView) {
            super();
            videoViewRef = new WeakReference<>(videoView);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SHOW_SEEKING:
                    Version1VideoView videoView = videoViewRef.get();
                    if (videoView != null && videoView.onSeekStartListener != null) {
                        videoView.onSeekStartListener.seekStarted();
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
