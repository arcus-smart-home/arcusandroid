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

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewStub;

import com.google.common.base.Strings;
import arcus.app.R;

public abstract class HeaderContentPopup extends ArcusFloatingFragment {
    private View headerBlockView;
    private View contentDividerView;
    private View contentBlockView;

    @Override
    public void setFloatingTitle() {
        if (Strings.isNullOrEmpty(getTitle())) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override
    public final void doContentSection() {
        ViewStub headerBlock = (ViewStub) contentView.findViewById(R.id.header_block);
        ViewStub contentDivider = (ViewStub) contentView.findViewById(R.id.header_content_divider);
        ViewStub contentBlock = (ViewStub) contentView.findViewById(R.id.content_block);

        Integer headerLayout = headerSectionLayout();
        if (headerLayout != null) {
            headerBlock.setLayoutResource(headerLayout);
            headerBlockView = headerBlock.inflate();
            setupHeaderSection(headerBlockView);
        }

        Integer dividerLayout = contentDividerLayout();
        if (dividerLayout != null) {
            contentDivider.setLayoutResource(dividerLayout);
            contentDividerView = contentDivider.inflate();
            setupDividerSection(contentDividerView);
        }

        Integer subContentLayout = subContentSectionLayout();
        if (subContentLayout != null) {
            contentBlock.setLayoutResource(subContentLayout);
            contentBlockView = contentBlock.inflate();
            setupSubContentSection(contentBlockView);
        }
    }

    public abstract void setupHeaderSection(View view);
    public abstract void setupDividerSection(View view);
    public abstract void setupSubContentSection(View view);
    @Nullable
    public abstract Integer headerSectionLayout();
    @Nullable
    public abstract Integer subContentSectionLayout();

    @Nullable @LayoutRes
    public Integer contentDividerLayout() {
        return R.layout.floating_divider;
    }

    @Override @LayoutRes
    public Integer contentSectionLayout() {
        return R.layout.floating_content_with_header;
    }
}
