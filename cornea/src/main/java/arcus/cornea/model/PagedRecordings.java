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
package arcus.cornea.model;

import com.iris.client.model.RecordingModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PagedRecordings {
    private String token;
    private List<RecordingModel> recordingModels = Collections.emptyList();

    public PagedRecordings(List<RecordingModel> models, String nextToken) {
        this.token = nextToken;
        if (models != null) {
            this.recordingModels = new ArrayList<>(models);
        }
    }

    public String getToken() {
        return this.token;
    }

    public List<RecordingModel> getRecordingModels() {
        return Collections.unmodifiableList(recordingModels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PagedRecordings that = (PagedRecordings) o;

        if (token != null ? !token.equals(that.token) : that.token != null) {
            return false;
        }
        return !(recordingModels != null ? !recordingModels.equals(that.recordingModels) : that.recordingModels != null);

    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (recordingModels != null ? recordingModels.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PagedRecordings{" +
              "token='" + token + '\'' +
              ", recordingModels size=" + (recordingModels == null ? 0 : recordingModels.size()) +
              '}';
    }
}
