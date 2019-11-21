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
package arcus.cornea.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.IrisClient;
import com.iris.client.bean.EasCode;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.service.EasCodeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EASCodeProvider extends BaseNonModelProvider<EasCode> {
    private static final EASCodeProvider INSTANCE;
    static {
        INSTANCE = new EASCodeProvider();
    }

    private final Function<EasCodeService.ListEasCodesResponse, List<EasCode>> transform =
          new Function<EasCodeService.ListEasCodesResponse, List<EasCode>>() {
              @Override public List<EasCode> apply(EasCodeService.ListEasCodesResponse input) {
                  List<EasCode> codes = new ArrayList<>(input.getEasCodes().size() + 1);
                  for (Map<String, Object> code : input.getEasCodes()) {
                      codes.add(new EasCode(code));
                  }
                  return codes;
              }
          };
    private final EasCodeService easCodeService;

    public static EASCodeProvider instance() {
        return INSTANCE;
    }

    public EASCodeProvider() {
        this(CorneaClientFactory.getClient(), CorneaClientFactory.getService(EasCodeService.class));
    }

    public EASCodeProvider(IrisClient client, EasCodeService service) {
        super(client);
        this.easCodeService = service;
    }

    @NonNull
    public List<EasCode> getAllEasCodes() {
        return getAll().or(Collections.<EasCode>emptyList());
    }

    @Nullable
    public EasCode getByCode(String easCode) {
        if (easCode == null || easCode.isEmpty()) {
            return null;
        }

        List<EasCode> codes = getAll().or(Collections.<EasCode>emptyList());
        for (EasCode code : codes) {
            if (easCode.equalsIgnoreCase(code.getEas())) {
                return code;
            }
        }

        return null;
    }

    @Override
    protected ClientFuture<List<EasCode>> doLoad() {
        return Futures.transform(easCodeService.listEasCodes(), transform);
    }
}
