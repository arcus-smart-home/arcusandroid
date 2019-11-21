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
package arcus.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Strings;
import arcus.cornea.subsystem.cameras.CameraPreviewGetter;
import arcus.cornea.subsystem.cameras.model.PlaybackModel;
import arcus.app.R;
import arcus.app.common.utils.VideoUtils;
import arcus.app.common.view.ScleraTextView;
import arcus.presentation.cameras.KeepAwakeController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class VideoActivity extends AppCompatActivity {
    private static final Logger logger = LoggerFactory.getLogger(VideoActivity.class);

    private static final BandwidthMeter DEFAULT_BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static final String ARG_PLAYBACK_MODEL = "ARG_PLAYBACK_MODEL";
    private static final String ARG_PLAYBACK_TIMESTAMP = "ARG_PLAYBACK_TIMESTAMP";

    public static Intent getLaunchIntent(
            @NonNull Context context,
            @NonNull PlaybackModel playbackModel) {
        return getLaunchIntent(context, playbackModel, "");
    }

    public static Intent getLaunchIntent(
            @NonNull Context context,
            @NonNull PlaybackModel playbackModel,
            @NonNull String timestamp) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(ARG_PLAYBACK_MODEL, playbackModel);
        intent.putExtra(ARG_PLAYBACK_TIMESTAMP, timestamp);

        return intent;
    }

    private PlaybackModel playbackModel;
    private String timeStamp = "";
    private boolean seekToEnd = true;
    private KeepAwakeController keepAwakeController;

    private ProgressBar progressBar;
    private ProgressDialog loadingDialog;

    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private final Player.EventListener eventListener = new Player.DefaultEventListener() {
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_BUFFERING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_READY:
                    if (seekToEnd) {
                        seekToEnd = false;

                        switch (playbackModel.getType()) {
                            case STREAM:
                            case RECORDING:
                                long duration = player.getDuration();
                                player.seekTo(duration);
                                break;
                            default:
                                loadingDialog.hide();
                                progressBar.setVisibility(View.GONE);
                                break;
                        }
                    } else {
                        loadingDialog.hide();
                        progressBar.setVisibility(View.GONE);
                    }
                    break;
                case Player.STATE_ENDED:
                    switch (playbackModel.getType()) {
                        case STREAM:
                        case RECORDING:
                        default:
                            finish();
                            break;
                    }
                    break;
                default:
                    // No-Op
                    break;
            }
        }

        public void onLoadingChanged(boolean isLoading) {
            if (!isLoading) {
                loadingDialog.hide();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            logger.error("Error received - Probably a 416. Stream should terminate here.", error.getCause());
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            Serializable object = bundle.getSerializable(ARG_PLAYBACK_MODEL);
            if (object != null && object instanceof PlaybackModel) {
                playbackModel = (PlaybackModel) object;
            }
            timeStamp = bundle.getString(ARG_PLAYBACK_TIMESTAMP, "");
        }

        if (playbackModel == null || Strings.isNullOrEmpty(playbackModel.getUrl())) {
            finish();
            return;
        }

        logger.debug("Could not create keep awake controller. functionality not implemented");

        progressBar = findViewById(R.id.progress_bar);

        ScleraTextView streamInfo = findViewById(R.id.stream_info);
        streamInfo.setText(timeStamp);

        final ScleraTextView streamClose = findViewById(R.id.stream_close);
        streamClose.setOnClickListener(v -> {
            sendStopStreaming();
            finish();
        });

        ScleraTextView streamTypeText = findViewById(R.id.stream_type_label);
        switch (playbackModel.getType()) {
            case STREAM:
                streamTypeText.setVisibility(View.VISIBLE);
                streamTypeText.setText(getString(R.string.live));
                streamClose.setText(getString(R.string.generic_end_text));
                break;

            case RECORDING:
                streamTypeText.setVisibility(View.VISIBLE);
                streamTypeText.setText(getString(R.string.camera_rec));
                streamClose.setText(getString(R.string.close_text));
                break;

            default:
            case CLIP:
                seekToEnd = false;
                streamClose.setText(getString(R.string.generic_end_text));
                break;
        }

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle(getString(R.string.video_player_loading));
        loadingDialog.setMessage(getString(R.string.video_player_loading_text));
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }

        sendStopStreaming();
        if (loadingDialog != null) {
            loadingDialog.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializePlayer() {
        CameraPreviewGetter.instance().pauseUpdates();

        PlayerView video = findViewById(R.id.video_view);
        video.setUseController(playbackModel.getType() == PlaybackModel.PlaybackType.CLIP);
        video.requestFocus();

        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(DEFAULT_BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(trackSelectionFactory),
                new DefaultLoadControl()
        );

        video.setPlayer(player);

        String userAgent = Util.getUserAgent(this, getPackageName());
        DataSource.Factory dsf = new DefaultDataSourceFactory(this, userAgent);
        MediaSource mediaSource = new HlsMediaSource.Factory(dsf).createMediaSource(Uri.parse(playbackModel.getUrl()));

        player.prepare(mediaSource);
        player.addListener(eventListener);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(eventListener);
            player.release();
            player = null;
        }

        CameraPreviewGetter.instance().resumeUpdates();
    }

    private void sendStopStreaming() {
        // Still check for null on exit since if we're called without a model during lifecycle onStop method this will still be called.
        if (playbackModel != null
                && PlaybackModel.PlaybackType.STREAM.equals(playbackModel.getType()) // This is a STREAM
                && playbackModel.isNewStream() // Only send stop if we're a new stream though
        ) {
            logger.debug("Sending STOP STREAM for Recording ID [{}]", playbackModel.getRecordingID());
            VideoUtils.sendStopStreaming(playbackModel.getRecordingID());
        }
    }
}
