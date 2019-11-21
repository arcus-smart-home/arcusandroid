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
package arcus.app.subsystems.favorites.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import arcus.cornea.subsystem.favorites.FavoritesController;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.activities.GenericConnectedFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.dashboard.settings.favorites.FavoritesListFragment;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.subsystems.favorites.adapters.FavoriteItemModel;
import arcus.app.subsystems.favorites.adapters.FavoritesListAdapter;
import arcus.app.subsystems.favorites.cards.FavoritesCard;

import java.util.ArrayList;
import java.util.List;


public class FavoritesCardItemView extends RecyclerView.ViewHolder {

    private RecyclerView mRecyclerView;
    private ImageView favoritesIcon;
    private FavoritesListAdapter mBasicListAdapter;
    private Context context;
    private float touchX = -1.0f;

    RecyclerView.OnItemTouchListener itemClickListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if(e.getAction() == MotionEvent.ACTION_DOWN) {
                touchX = e.getX();
                return false;
            }
            if (childView != null && e.getAction() == MotionEvent.ACTION_UP && Math.abs(touchX-e.getX()) < 15) {
                onItemClick(rv.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    };

    public FavoritesCardItemView(View view) {
        super(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        favoritesIcon = view.findViewById(R.id.favorites_icon);

        context = view.getContext();
    }

    public void build(@NonNull FavoritesCard card) {
        List<Model> models = card.getModels();
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setAdapter(getAdapter(models));
        mRecyclerView.invalidate();
        mRecyclerView.removeOnItemTouchListener(itemClickListener);
        mRecyclerView.addOnItemTouchListener(itemClickListener);

        ImageManager.with(context)
            .putDrawableResource(R.drawable.favorite_light_22x20)
            .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
            .into(favoritesIcon)
            .execute();
    }

    @NonNull
    private RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        return linearLayoutManager;
    }

    private RecyclerView.Adapter getAdapter(@NonNull List<Model> models) {
        mBasicListAdapter = new FavoritesListAdapter(context);

        addData(models);

        return mBasicListAdapter;
    }

    public void onItemClick(int position) {
        // If this is the last item (the Add A Favorite card), launch Manage Favorites
        if(position == mBasicListAdapter.getItemCount() - 1){
            context.startActivity(GenericConnectedFragmentActivity
                .getLaunchIntent(
                    context,
                    FavoritesListFragment.class,
                    null,
                    false
                )
            );
            return;
        }

        final FavoriteItemModel item = mBasicListAdapter.getItem(position);

        // Ignore clicks on disabled items
        if (item.isDisabled()) {
            return;
        }

        String itemType = FavoritesController.determineModelType(item.getModel().getAddress());
        if (FavoritesController.DEVICE_MODEL.equals(itemType)) {
            int deviceIndex = SessionModelManager.instance().indexOf((DeviceModel) item.getModel(), true);
            if (deviceIndex == -1) return;
            BackstackManager.withAnimation(TransitionEffect.FADE)
                    .navigateToFragment(DeviceDetailParentFragment.newInstance(deviceIndex), true);
        }
        else if (FavoritesController.SCENE_MODEL.equals(itemType)) {
            ((SceneModel) item.getModel()).fire();
            item.setDisabled(true);
            mBasicListAdapter.notifyItemRangeChanged(0, mBasicListAdapter.getItemCount());

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    item.setDisabled(false);
                    mBasicListAdapter.notifyItemRangeChanged(0, mBasicListAdapter.getItemCount());
                }
            }, 3000);
        }
    }

    private void addData(@NonNull List<Model> models) {
        List<FavoriteItemModel> data = new ArrayList<>();

        // Iterate and cast for specific model
        for (Model model : models) {
            if (model instanceof DeviceModel) {
                data.add(new FavoriteItemModel((DeviceModel) model));

            } else if (model instanceof SceneModel) {
                data.add(new FavoriteItemModel((SceneModel) model));
            }
        }

        mBasicListAdapter.setData(data);
    }

}
