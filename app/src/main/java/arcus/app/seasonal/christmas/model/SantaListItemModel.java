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
package arcus.app.seasonal.christmas.model;

public class SantaListItemModel {
    private String name;
    private String description;
    private String imageURL;
    private String modelID;
    private boolean currentlyChecked;
    private int imageResource;
    private boolean withChevron = false;

    public SantaListItemModel(String name, boolean currentlyChecked) {
        this.name = name;
        this.currentlyChecked = currentlyChecked;

        this.description = "";
        this.imageURL = "";
        this.imageResource  = -1;
    }

    public SantaListItemModel(String name, String description, String modelID, String imageURL, boolean currentlyChecked) {
        this.name = name;
        this.description = description;
        this.modelID = modelID;
        this.imageURL = imageURL;
        this.imageResource  = -1;
        this.currentlyChecked = currentlyChecked;
    }

    public SantaListItemModel(String name, String description, int imageResource, boolean currentlyChecked) {
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;
        this.modelID = name;
        this.imageURL = "";
        this.currentlyChecked = currentlyChecked;
    }

    public SantaListItemModel(String name, String description, int imageResource, boolean currentlyChecked, boolean withChevy) {
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;
        this.modelID = name;
        this.imageURL = "";
        this.currentlyChecked = currentlyChecked;
        this.withChevron = withChevy;
    }

    public boolean isWithChevron() {
        return withChevron;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public boolean isCurrentlyChecked() {
        return currentlyChecked;
    }

    public void setCurrentlyChecked(boolean currentlyChecked) {
        this.currentlyChecked = currentlyChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SantaListItemModel that = (SantaListItemModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        return !(modelID != null ? !modelID.equals(that.modelID) : that.modelID != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (modelID != null ? modelID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SantaListItemModel{" +
              "name='" + name + '\'' +
              ", description='" + description + '\'' +
              ", imageURL='" + imageURL + '\'' +
              ", modelID='" + modelID + '\'' +
              ", currentlyChecked=" + currentlyChecked +
              ", imageResource=" + imageResource +
              ", withChevron=" + withChevron +
              '}';
    }
}
