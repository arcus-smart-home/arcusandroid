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
package arcus.app.account.settings.walkthroughs;


public class SetupWalkthroughs {

    private int mTitleResId;
    private int mLayoutResId;
    private int mDescriptionResId;
    private int mPictureResID;

    SetupWalkthroughs(int layoutResId, int titleResId, int descriptionResId, int pictureResID) {
        mTitleResId = titleResId;
        mDescriptionResId = descriptionResId;
        mLayoutResId = layoutResId;
        mPictureResID = pictureResID;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getDescriptionId() {
        return mDescriptionResId;
    }

    public int getLayoutResId(){
        return mLayoutResId;
    }

    public int getImageId() {
        return mPictureResID;
    }

}

