/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.statefun.examples.shoppingcart;

import com.google.auto.service.AutoService;
import java.util.Map;
import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.io.EgressSpec;
import org.apache.flink.statefun.sdk.io.IngressSpec;
import org.apache.flink.statefun.sdk.kafka.KafkaEgressBuilder;
import org.apache.flink.statefun.sdk.kafka.KafkaIngressBuilder;
import org.apache.flink.statefun.sdk.spi.StatefulFunctionModule;
import java.util.logging.Logger;

@AutoService(StatefulFunctionModule.class)
public class ShoppingCartModule implements StatefulFunctionModule {
  @Override
  public void configure(Map<String, String> globalConfiguration, Binder binder) {
    // bind functions
    FunctionProvider provider = new FunctionProvider();

    binder.bindFunctionProvider(Identifiers.USER, provider);
    binder.bindFunctionProvider(Identifiers.INVENTORY, provider);

    binder.bindIngress(KafkaSpecs.ADD_TO_CART_SPEC);
    binder.bindIngressRouter(Identifiers.ADD_TO_CART, new AddToCartRouter());

    binder.bindIngress(KafkaSpecs.CLEAR_CART_SPEC);
    binder.bindIngressRouter(Identifiers.CLEAR_CART, new ClearCartRouter());

    binder.bindIngress(KafkaSpecs.CHECKOUT_SPEC);
    binder.bindIngressRouter(Identifiers.CHECKOUT, new CheckoutRouter());

    binder.bindIngress(KafkaSpecs.RESTOCK_ITEM_SPEC);
    binder.bindIngressRouter(Identifiers.RESTOCK_ITEM, new RestockItemRouter());

    binder.bindEgress(KafkaSpecs.RECEIPT_SPEC);

  }
}
