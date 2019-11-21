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
package arcus.app.device.removal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Product;
import com.iris.client.capability.ProductCatalog;
import com.iris.client.event.Listener;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.removal.controller.DeviceRemovalSequenceController;

import java.util.List;
import java.util.Map;


public class UnpairingZWaveDeviceFragment extends SequencedFragment<DeviceRemovalSequenceController> {

    private final static String PRODUCT_ID = "PRODUCT_ID";

    private Version1Button cancelButton;
    private Version1TextView unpairingInstructions;
    private Version1TextView unpairingHeader;

    @NonNull
    public static UnpairingZWaveDeviceFragment newInstance (String productId) {
        UnpairingZWaveDeviceFragment instance = new UnpairingZWaveDeviceFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PRODUCT_ID, productId);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        cancelButton = (Version1Button) view.findViewById(R.id.cancel_button);
        unpairingInstructions = (Version1TextView) view.findViewById(R.id.device_specific_removal_copy);
        unpairingHeader = (Version1TextView) view.findViewById(R.id.device_remove_searching);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().cancel();
            }
        });

        fetchProductUnpairingInstructions();
    }


    @Override
    public String getTitle() {
        return getString(R.string.device_remove_device);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_unpairing_zwave_device;
    }

    private void fetchProductUnpairingInstructions() {
        String productId = getArguments().getString(PRODUCT_ID);
        if (!StringUtils.isEmpty(productId)) {
            String productAddress = Addresses.toObjectAddress(Product.NAMESPACE, productId);
            ModelSource<ProductModel> modelSource = ProductModelProvider.instance().getModel(productAddress);

            if (modelSource.isLoaded()) {
                logger.debug("Product model was cached; unpacking instructions for [{}].", productId);
                unpackUnpairingInstructionsCopy(modelSource.get().getRemoval());
            } else {
                logger.debug("Product model was not cached; loading for [{}].", productId);
                // ProductModelProvider is not loading based on GetAttributes. Using product catalog method.
                try {
                    ProductCatalog.GetProductRequest request = new ProductCatalog.GetProductRequest();
                    request.setAddress(Addresses.toServiceAddress(ProductCatalog.NAMESPACE));
                    request.setId(productId);

                    CorneaClientFactory.getClient().request(request)
                          .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                              @Override
                              public void onEvent(ClientEvent clientEvent) {
                                  ProductCatalog.GetProductResponse response = new ProductCatalog.GetProductResponse(clientEvent);
                                  if (response.getProduct() != null) {
                                      ProductModel model = (ProductModel) CorneaClientFactory.getModelCache().addOrUpdate(response.getProduct());
                                      if (model != null) {
                                          unpackUnpairingInstructionsCopy(model.getRemoval());
                                      }
                                  }
                              }
                          }));
                }
                catch (Exception ex) {
                    // User can still unpair, just won't have instructions.
                    logger.debug("Cannot request product", ex);
                }
            }
        }
    }

    private void unpackUnpairingInstructionsCopy (@Nullable List<Map<String, Object>> unpairingData) {

        if (unpairingData == null || unpairingData.size() == 0 || unpairingData.get(0).get("text") == null) {
            logger.debug("No un-pairing instructions found for device.");
            unpairingInstructions.setVisibility(View.INVISIBLE);
        } else {

            Map<String, Object> unpairingDataFound = unpairingData.get(0);
            String unparingInstructionsCopy = unpairingDataFound.get("text").toString().replace(".  ",".\n");
            logger.debug("Got un-pairing instruction: '{}'", unparingInstructionsCopy);
            unpairingInstructions.setText(unparingInstructionsCopy);
            unpairingInstructions.setVisibility(View.VISIBLE);

            //There is a title for this device
            if (!(unpairingData.get(0).get("title") == null)) {
                String unparingTitleCopy = unpairingDataFound.get("title").toString();
                unpairingHeader.setText(unparingTitleCopy);
            }

        }
    }
}
