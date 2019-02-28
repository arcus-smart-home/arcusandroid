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
package arcus.cornea.model.smartystreets;

import com.google.gson.annotations.SerializedName;

public class SmartyStreetsResponse {
//    @SerializedName("input_id") private String inputId;
//    @SerializedName("input_index") private Number inputIndex;
//    @SerializedName("candidate_index") private Number candidateIndex;
//    @SerializedName("addressee") private String addressee;
//    @SerializedName("delivery_line_1") private String deliveryLine1;
//    @SerializedName("delivery_line_2") private String deliveryLine2;
//    @SerializedName("last_line") private String last_line;
//    @SerializedName("delivery_point_barcode") private String deliveryPointBarcode;
//    @SerializedName("components") private Components components;
//    @SerializedName("analysis") private Analysis analysis;

    // This is really the only one we need from the response right now.
    @SerializedName("metadata") private Metadata metadata;

    public SmartyStreetsResponse() {
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SmartyStreetsResponse that = (SmartyStreetsResponse) o;

        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;

    }

    @Override public int hashCode() {
        return metadata != null ? metadata.hashCode() : 0;
    }

    @Override public String toString() {
        return "SmartyStreetsResponse{" +
              "metadata=" + metadata +
              '}';
    }
}
