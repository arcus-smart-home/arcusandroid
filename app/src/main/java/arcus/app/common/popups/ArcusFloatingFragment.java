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
package arcus.app.common.popups;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1TextView;

import java.lang.ref.WeakReference;

public abstract class ArcusFloatingFragment extends BaseFragment implements View.OnClickListener{

    public interface OnCloseHandler {
        void onClose();
    }

    public interface OnOpenHandler {
        void onOpen(ArcusFloatingFragment fragment);
    }

    private boolean hasCloseButton = true;

    protected ImageView closeBtn;
    protected ImageView titleLogo;
    protected TextView title;
    protected ViewStub contentViewStub;
    protected View contentView;
    protected View floatingContainer;
    protected OnCloseHandler onCloseHandler;
    protected Version1TextView doneBtn;
    private boolean initiallyFullScreen = false;
    protected WeakReference<OnOpenHandler> onOpenHandlerRef = new WeakReference<>(null);

    public ArcusFloatingFragment() {
        hasCloseButton = true;
    }

    public ArcusFloatingFragment(boolean withCloseButton) {
        hasCloseButton = withCloseButton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        floatingContainer = view.findViewById(R.id.fragment_popup_content_container);
        doneBtn = (Version1TextView) view.findViewById(R.id.fragment_arcus_pop_up_done);
        closeBtn = (ImageView) view.findViewById(R.id.fragment_arcus_pop_up_close_btn);
        titleLogo = (ImageView) view.findViewById(R.id.title_logo);

        closeBtn.setOnClickListener(this);
        final View closeBtnParent = (View) closeBtn.getParent();
        if (closeBtnParent != null) {
            closeBtn.setVisibility(hasCloseButton ? View.VISIBLE : View.GONE);

            closeBtnParent.post(new Runnable() {
                @Override
                public void run() {
                    Rect biggerTouchArea = new Rect();
                    if (closeBtn == null) {
                        return;
                    }

                    closeBtn.getHitRect(biggerTouchArea);
                    biggerTouchArea.left -= 100;
                    biggerTouchArea.top -= 50;
                    biggerTouchArea.bottom += 50;
                    closeBtnParent.setTouchDelegate(new TouchDelegate(biggerTouchArea, closeBtn));
                }
            });
        }

        if(doneBtn != null) {
            doneBtn.setOnClickListener(this);
        }

        contentViewStub = (ViewStub) view.findViewById(R.id.fragment_arcus_pop_up_content);

        title = (TextView) view.findViewById(R.id.fragment_arcus_pop_up_title);

        load();

        if (onOpenHandlerRef != null && onOpenHandlerRef.get() != null) {
            onOpenHandlerRef.get().onOpen(this);
        }

        //todo: we might want to limit height to half of the screen size
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Reshow the activity bar when we disappear
        showFullScreen(initiallyFullScreen);
    }

    private void load(){

        setFloatingTitle();

        initiallyFullScreen = isFullScreen();

        if(contentSectionLayout() !=null){
            contentViewStub.setLayoutResource(contentSectionLayout());
            contentView = contentViewStub.inflate();
            doContentSection();
        }

        if(floatingBackgroundColor() != -1 ){
            floatingContainer.setBackgroundColor(floatingBackgroundColor());
        }
    }

    public void setHasCloseButton(boolean hasCloseButton) {
        closeBtn.setVisibility(hasCloseButton ? View.VISIBLE : View.GONE);
    }

    public void setHasDoneButton(boolean hasDoneButton) {
        if(doneBtn == null) {
            return;
        }
        if(hasDoneButton) {
            doneBtn.setVisibility(View.VISIBLE);
            closeBtn.setVisibility(View.GONE);
        } else {
            doneBtn.setVisibility(View.GONE);
            closeBtn.setVisibility(View.VISIBLE);
        }

    }

    public void setCloseButtonIcon(@DrawableRes int buttonIcon) {
        if (closeBtn == null) {
            return;
        }

        closeBtn.setImageResource(buttonIcon);
    }

    public void doClose(){
    }

    public Integer floatingBackgroundColor(){ return -1; }

    public abstract void setFloatingTitle();

    public abstract void doContentSection();

    public abstract Integer contentSectionLayout();

    public OnCloseHandler getOnCloseHandler() {
        return onCloseHandler;
    }

    public void setOnCloseHandler(OnCloseHandler onCloseHandler) {
        this.onCloseHandler = onCloseHandler;
    }

    public void setOnOpenHandler(OnOpenHandler onOpenHandler) {
        this.onOpenHandlerRef = new WeakReference<>(onOpenHandler);
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.fragment_arcus_pop_up_close_btn:
            case R.id.fragment_arcus_pop_up_done:
                doClose();
                BackstackManager.getInstance().navigateBack();
                fireOnClose();
                break;
        }
    }

    protected void fireOnClose() {
        if (onCloseHandler != null) {
            onCloseHandler.onClose();
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_arcus_pop_up;
    }

    protected void showTitle(boolean isVisible) {
        title.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    protected void showTitleLogo(boolean isVisible) {
        titleLogo.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    protected void showFullScreen(boolean fullscreen) {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return;
        }

        ActionBar actionBar = ((BaseActivity)activity).getSupportActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                actionBar.hide();
            }
            else {
                actionBar.show();
            }
        }
    }

    private boolean isFullScreen() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return true;
        }

        ActionBar actionBar = ((BaseActivity)activity).getSupportActionBar();
        if (actionBar != null) {
            return !actionBar.isShowing();
        }
        return true;
    }
}
