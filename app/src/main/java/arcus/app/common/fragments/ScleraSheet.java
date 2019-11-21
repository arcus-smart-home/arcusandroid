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
package arcus.app.common.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.ScleraTransitionManager;
import arcus.app.common.sequence.SequenceController;
import arcus.app.common.view.ScleraTextView;

import org.apache.commons.lang3.StringUtils;



public abstract class ScleraSheet<T extends SequenceController> extends FullscreenFragment<T> {

    public abstract int getSheetLayoutId();

    private ImageView closeBox;
    private ImageView arcusIconTitle;
    private ScleraTextView textTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View sheet = super.onCreateView(inflater, container, savedInstanceState);

        this.closeBox = (ImageView) sheet.findViewById(R.id.close);
        this.arcusIconTitle = (ImageView) sheet.findViewById(R.id.arcus_icon_title);
        this.textTitle = (ScleraTextView) sheet.findViewById(R.id.text_title);

        // Inflate the sheet's contents
        ViewStub sheetContents = (ViewStub) sheet.findViewById(R.id.sclera_sheet_contents);
        sheetContents.setLayoutResource(getSheetLayoutId());
        sheetContents.inflate();

        return sheet;
    }

    @Override
    public void onResume() {
        super.onResume();

        textTitle.setText(getTitle());
        closeBox.setVisibility(hasCloseBox() ? View.VISIBLE : View.INVISIBLE);
        arcusIconTitle.setVisibility(StringUtils.isEmpty(getTitle()) ? View.VISIBLE : View.INVISIBLE);
        textTitle.setVisibility(StringUtils.isEmpty(getTitle()) ? View.INVISIBLE : View.VISIBLE);
        closeBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_sclera_sheet;
    }

    public boolean hasCloseBox() {
        return false;
    }

    public void dismiss() {
        if (onShouldDismiss()) {
            ScleraTransitionManager.dismissSheet();
        }
    }

    /**
     * Determine if this sheet should dismiss when requested. Override in subclasses to trap dismiss
     * behavior to add features like an "Are you sure?" prompt.
     *
     * @return True to indicate sheet should dismiss; false to prevent it from closing.
     */
    protected boolean onShouldDismiss() {
        return true;
    }
}
