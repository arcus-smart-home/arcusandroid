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
package arcus.app.common.adapters;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import arcus.app.R;
import arcus.app.common.cards.AlertCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.TopImageCard;
import arcus.app.common.cards.view.AlertCardItemView;
import arcus.app.common.cards.view.DashboardCenteredTextCardItemView;
import arcus.app.common.cards.view.TopImageCardItemView;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.alarm.promonitoring.cards.ProMonitoringDashboardCard;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringDashboardCardItemView;
import arcus.app.subsystems.alarm.safety.cards.SafetyAlarmCard;
import arcus.app.subsystems.alarm.safety.view.SafetyAlarmCardItemView;
import arcus.app.subsystems.alarm.security.cards.SecurityAlarmCard;
import arcus.app.subsystems.alarm.security.view.SecurityAlarmCardItemView;
import arcus.app.subsystems.camera.cards.CameraCard;
import arcus.app.subsystems.camera.views.CameraCardItemView;
import arcus.app.subsystems.care.cards.CareCard;
import arcus.app.subsystems.care.view.CareCardItemView;
import arcus.app.subsystems.climate.cards.ClimateCard;
import arcus.app.subsystems.climate.views.ClimateCardItemView;
import arcus.app.subsystems.comingsoon.cards.ComingSoonCard;
import arcus.app.subsystems.comingsoon.view.ComingSoonCardView;
import arcus.app.subsystems.doorsnlocks.cards.DoorsNLocksCard;
import arcus.app.subsystems.doorsnlocks.view.DoorsNLocksCardItemView;
import arcus.app.subsystems.favorites.cards.FavoritesCard;
import arcus.app.subsystems.favorites.cards.NoFavoritesCard;
import arcus.app.subsystems.favorites.view.FavoritesCardItemView;
import arcus.app.subsystems.feature.cards.FeatureCard;
import arcus.app.subsystems.feature.views.FeatureCardItemView;
import arcus.app.subsystems.history.cards.HistoryCard;
import arcus.app.subsystems.history.view.HistoryCardItemView;
import arcus.app.subsystems.homenfamily.cards.HomeNFamilyCard;
import arcus.app.subsystems.homenfamily.view.HomeNFamilyCardItemView;
import arcus.app.subsystems.lawnandgarden.cards.LawnAndGardenCard;
import arcus.app.subsystems.lawnandgarden.views.LawnAndGardenCardItemView;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;
import arcus.app.subsystems.learnmore.view.LearnMoreCardView;
import arcus.app.subsystems.lightsnswitches.cards.LightsNSwitchesCard;
import arcus.app.subsystems.lightsnswitches.view.LightsNSwitchesCardItemView;
import arcus.app.subsystems.water.cards.WaterCard;
import arcus.app.subsystems.water.views.WaterCardItemView;

import java.util.ArrayList;


public class HomeFragmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SimpleDividerCard> dashboardCards = new ArrayList<>();
    private final int FAVORITES = 1, HISTORY = 2, LIGHTS_AND_SWITCHES = 3, ALERTS = 4, SECURITY_ALARM = 5,
            CLIMATE = 6, DOORS_AND_LOCKS = 7, HOME_AND_FAMILY = 8, SAFETY_ALARM = 9, CAMERAS = 10, WINDOWS_AND_BLINDS = 11,
            LAWN_AND_GARDEN = 12, CARE = 13, WATER = 14, ENERGY = 15, TOP_CARD = 16, LEARNMORE = 17, COMING_SOON = 18, NO_FAVORITES = 20, FEATURE = 21;

    public HomeFragmentRecyclerAdapter(ArrayList<SimpleDividerCard> dashboardCards) {
        this.dashboardCards = dashboardCards;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch (viewType) {
            case CLIMATE:
                view = inflater.inflate(R.layout.dashboard_subsystem_climate, viewGroup, false);
                viewHolder = new ClimateCardItemView(view);
                break;
            case TOP_CARD:
                view = inflater.inflate(R.layout.card_top_image, viewGroup, false);
                viewHolder = new TopImageCardItemView(view);
                break;
            case DOORS_AND_LOCKS:
                view = inflater.inflate(R.layout.dashboard_subsystem_doorsnlocks, viewGroup, false);
                viewHolder = new DoorsNLocksCardItemView(view);
                break;
            case FAVORITES:
                view = inflater.inflate(R.layout.dashboard_subsystem_favorites, viewGroup, false);
                viewHolder = new FavoritesCardItemView(view);
                break;
            case CAMERAS:
                view = inflater.inflate(R.layout.dashboard_subsystem_camera, viewGroup, false);
                viewHolder = new CameraCardItemView(view);
                break;
            case HOME_AND_FAMILY:
                view = inflater.inflate(R.layout.dashboard_subsystem_homenfamily, viewGroup, false);
                viewHolder = new HomeNFamilyCardItemView(view);
                break;
            case LIGHTS_AND_SWITCHES:
                view = inflater.inflate(R.layout.dashboard_subsystem_lightsnswitches, viewGroup, false);
                viewHolder = new LightsNSwitchesCardItemView(view);
                break;
            case LAWN_AND_GARDEN:
                view = inflater.inflate(R.layout.dashboard_subsystem_lawngarden, viewGroup, false);
                viewHolder = new LawnAndGardenCardItemView(view);
                break;
            case CARE:
                view = inflater.inflate(R.layout.dashboard_subsystem_care, viewGroup, false);
                viewHolder = new CareCardItemView(view);
                break;
            case WATER:
                view = inflater.inflate(R.layout.dashboard_subsystem_water, viewGroup, false);
                viewHolder = new WaterCardItemView(view);
                break;
            case ALERTS:
                view = inflater.inflate(R.layout.dashboard_subsystem_alert, viewGroup, false);
                viewHolder = new AlertCardItemView(view);
                break;
            case LEARNMORE:
                view = inflater.inflate(R.layout.dashboard_subsystem_learnmore, viewGroup, false);
                viewHolder = new LearnMoreCardView(view);
                break;
            case COMING_SOON:
                view = inflater.inflate(R.layout.dashboard_subsystem_comingsoon, viewGroup, false);
                viewHolder = new ComingSoonCardView(view);
                break;
            case HISTORY:
                view = inflater.inflate(R.layout.dashboard_subsystem_history, viewGroup, false);
                viewHolder = new HistoryCardItemView(view);
                break;
            case SECURITY_ALARM:
                view = inflater.inflate(R.layout.dashboard_subsystem_alarms, viewGroup, false);
                viewHolder = new ProMonitoringDashboardCardItemView(view);
                break;
            case NO_FAVORITES:
                view = inflater.inflate(R.layout.dashboard_centered_text, viewGroup, false);
                viewHolder = new DashboardCenteredTextCardItemView(view);
                break;
            case FEATURE:
                view = inflater.inflate(R.layout.dashboard_feature, viewGroup, false);
                viewHolder = new FeatureCardItemView(view);
                break;
            default:
                view = inflater.inflate(R.layout.service_card, viewGroup, false);
                viewHolder = new ClimateCardItemView(view);
                break;
        }
        return viewHolder;
    }

    public void flipCardRight(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DashboardFlipViewHolder) {
            //((DashboardFlipViewHolder) holder).flipCardRight();
        }
    }

    public void flipCardLeft(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DashboardFlipViewHolder) {
            //((DashboardFlipViewHolder) holder).flipCardLeft();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(dashboardCards.get(position) instanceof TopImageCard) {
            ((TopImageCardItemView)holder).build((TopImageCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof ClimateCard) {
            ((ClimateCardItemView)holder).build((ClimateCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof DoorsNLocksCard) {
            ((DoorsNLocksCardItemView)holder).build((DoorsNLocksCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof FavoritesCard) {
            ((FavoritesCardItemView)holder).build((FavoritesCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof SafetyAlarmCard) {
            ((SafetyAlarmCardItemView)holder).build((SafetyAlarmCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof SecurityAlarmCard) {
            ((SecurityAlarmCardItemView)holder).build((SecurityAlarmCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof CameraCard) {
            ((CameraCardItemView)holder).build((CameraCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof HomeNFamilyCard) {
            ((HomeNFamilyCardItemView)holder).build((HomeNFamilyCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof LightsNSwitchesCard) {
            ((LightsNSwitchesCardItemView)holder).build((LightsNSwitchesCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof LawnAndGardenCard) {
            ((LawnAndGardenCardItemView)holder).build((LawnAndGardenCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof CareCard) {
            ((CareCardItemView)holder).build((CareCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof WaterCard) {
            ((WaterCardItemView)holder).build((WaterCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof AlertCard) {
            ((AlertCardItemView)holder).build((AlertCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof LearnMoreCard) {
            ((LearnMoreCardView)holder).build((LearnMoreCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof ComingSoonCard) {
            ((ComingSoonCardView)holder).build((ComingSoonCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof HistoryCard) {
            ((HistoryCardItemView)holder).build((HistoryCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof ProMonitoringDashboardCard) {
            ((ProMonitoringDashboardCardItemView)holder).build((ProMonitoringDashboardCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof NoFavoritesCard) {
            ((DashboardCenteredTextCardItemView)holder).build((NoFavoritesCard)dashboardCards.get(position));
        } else if(dashboardCards.get(position) instanceof FeatureCard) {
            ((FeatureCardItemView)holder).build((FeatureCard)dashboardCards.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return this.dashboardCards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(dashboardCards.get(position) instanceof TopImageCard) {
            return TOP_CARD;
        } else if(dashboardCards.get(position) instanceof ClimateCard) {
            return CLIMATE;
        } else if(dashboardCards.get(position) instanceof DoorsNLocksCard) {
            return DOORS_AND_LOCKS;
        } else if(dashboardCards.get(position) instanceof FavoritesCard) {
            return FAVORITES;
        } else if(dashboardCards.get(position) instanceof SafetyAlarmCard) {
            return SAFETY_ALARM;
        } else if(dashboardCards.get(position) instanceof SecurityAlarmCard) {
            return SECURITY_ALARM;
        } else if(dashboardCards.get(position) instanceof CameraCard) {
            return CAMERAS;
        } else if(dashboardCards.get(position) instanceof HomeNFamilyCard) {
            return HOME_AND_FAMILY;
        } else if(dashboardCards.get(position) instanceof LightsNSwitchesCard) {
            return LIGHTS_AND_SWITCHES;
        } else if(dashboardCards.get(position) instanceof LawnAndGardenCard) {
            return LAWN_AND_GARDEN;
        } else if(dashboardCards.get(position) instanceof CareCard) {
            return CARE;
        } else if(dashboardCards.get(position) instanceof WaterCard) {
            return WATER;
        } else if(dashboardCards.get(position) instanceof AlertCard) {
            return ALERTS;
        } else if(dashboardCards.get(position) instanceof LearnMoreCard) {
            return LEARNMORE;
        } else if(dashboardCards.get(position) instanceof ComingSoonCard) {
            return COMING_SOON;
        } else if(dashboardCards.get(position) instanceof HistoryCard) {
            return HISTORY;
        } else if(dashboardCards.get(position) instanceof ProMonitoringDashboardCard) {
            return SECURITY_ALARM;
        } else if(dashboardCards.get(position) instanceof NoFavoritesCard) {
            return NO_FAVORITES;
        } else if(dashboardCards.get(position) instanceof FeatureCard) {
            return FEATURE;
        }
        return -1;
    }

    public void add(SimpleDividerCard card) {
        setCardType(card);
        dashboardCards.add(card);
        notifyDataSetChanged();
    }

    public void addAtStart(SimpleDividerCard card) {
        setCardType(card);
        dashboardCards.add(0, card);
        notifyDataSetChanged();
    }

    public void removeAll() {
        dashboardCards.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        dashboardCards.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dashboardCards.size());
    }

    /**
     * Attempts to update the first view bound to a card of the given type. If multiple cards
     * of the same type exist (i.e., like "Learn More") then only the first card in the list bound
     * to this model type will be updated.
     *
     * @param card The card model to be updated.
     * @return True if a view bound to this type of card is discovered and updated, false otherwise
     * (including if card is null or if no view is associated with this card type).
     */
    public boolean notifyCardChanged(@Nullable SimpleDividerCard card) {
        Integer positionOfCard = getPositionOfCard(card);

        if (positionOfCard != null) {
            dashboardCards.remove(positionOfCard.intValue());
            dashboardCards.add(positionOfCard.intValue(), card);
            notifyItemChanged(positionOfCard);
            return true;
        }

        // No item of this model type in list
        else {
            return false;
        }
    }

    /**
     * Attempts to the get the position of the first item in the list whose view is bound to the
     * card of the given type.
     *
     * @param card The card model whose view position should be determined.
     * @return The position of the first view item bound to this card type.
     */
    @Nullable public Integer getPositionOfCard(@Nullable Card card) {

        if (card == null) {
            return null;
        }

        for (int index = 0; index < getItemCount(); index++) {
            if (dashboardCards.get(index).getTag().equals(card.getTag())) {
                return index;
            }
        }

        return null;
    }
    private void setCardType(SimpleDividerCard card) {
        if(card instanceof TopImageCard) {
            card.setType(TOP_CARD);
        } else if(card instanceof ClimateCard) {
            card.setType(CLIMATE);
        } else if(card instanceof DoorsNLocksCard) {
            card.setType(DOORS_AND_LOCKS);
        } else if(card instanceof FavoritesCard) {
            card.setType(FAVORITES);
        } else if(card instanceof CameraCard) {
            card.setType(CAMERAS);
        } else if(card instanceof HomeNFamilyCard) {
            card.setType(HOME_AND_FAMILY);
        } else if(card instanceof LightsNSwitchesCard) {
            card.setType(LIGHTS_AND_SWITCHES);
        } else if(card instanceof LawnAndGardenCard) {
            card.setType(LAWN_AND_GARDEN);
        } else if(card instanceof CareCard) {
            card.setType(CARE);
        } else if(card instanceof WaterCard) {
            card.setType(WATER);
        } else if(card instanceof AlertCard) {
            card.setType(ALERTS);
        } else if(card instanceof LearnMoreCard) {
            card.setType(getLearnMoreType((LearnMoreCard)card));
        } else if(card instanceof ComingSoonCard) {
            card.setType(COMING_SOON);
        } else if(card instanceof HistoryCard) {
            card.setType(HISTORY);
        } else if(card instanceof ProMonitoringDashboardCard) {
            card.setType(SECURITY_ALARM);
        } else if(card instanceof NoFavoritesCard) {
            card.setType(FAVORITES);
        }
    }


    private int getLearnMoreType(LearnMoreCard card) {

        if(card.getTag().equals(ServiceCard.CLIMATE.toString())) {
            return CLIMATE;
        } else if(card.getTag().equals(ServiceCard.DOORS_AND_LOCKS.toString())) {
            return DOORS_AND_LOCKS;
        } else if(card.getTag().equals(ServiceCard.FAVORITES.toString())) {
            return FAVORITES;
        } else if(card.getTag().equals(ServiceCard.CAMERAS.toString())) {
            return CAMERAS;
        } else if(card.getTag().equals(ServiceCard.HOME_AND_FAMILY.toString())) {
            return HOME_AND_FAMILY;
        } else if(card.getTag().equals(ServiceCard.LIGHTS_AND_SWITCHES.toString())) {
            return LIGHTS_AND_SWITCHES;
        } else if(card.getTag().equals(ServiceCard.LAWN_AND_GARDEN.toString())) {
            return LAWN_AND_GARDEN;
        } else if(card.getTag().equals(ServiceCard.CARE.toString())) {
            return CARE;
        } else if(card.getTag().equals(ServiceCard.WATER.toString())) {
            return WATER;
        } else if(card.getTag().equals(ServiceCard.HISTORY.toString())) {
            return HISTORY;
        } else if(card.getTag().equals(ServiceCard.SECURITY_ALARM.toString())) {
            return SECURITY_ALARM;
        } else if(card.getTag().equals(ServiceCard.FAVORITES.toString())) {
            return FAVORITES;
        }
        return -1;
    }
}
