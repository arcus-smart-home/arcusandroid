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
package arcus.app.common.controller;

import android.content.Context;
import androidx.annotation.Nullable;

import com.dexafree.materialList.model.Card;


public abstract class AbstractCardController<T extends Card> {


    private Context mContext;

    @Nullable
    private Callback mDelegate;

    private T mCurrentCard;

    public AbstractCardController(Context context) {
        this.mContext = context;
    }

    @Nullable
    public abstract T getCard();

    protected void setCurrentCard(T card) {
        mCurrentCard = card;
        if (mDelegate != null) mDelegate.updateCard(card);
    }

    protected T getCurrentCard() {
        return mCurrentCard;
    }

    protected Context getContext() {
        return mContext;
    }

    public void setCallback(Callback delegate) {
        this.mDelegate = delegate;
    }

    public void removeCallback() {
        this.mDelegate = null;
    }
    
    public interface Callback {
        void updateCard(Card card);
    }
}
