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

import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.Address;
import org.apache.flink.statefun.sdk.Context;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.StatefulFunction;
import org.apache.flink.statefun.sdk.annotations.Persisted;
import org.apache.flink.statefun.sdk.state.PersistedTable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class UserShoppingCart implements StatefulFunction {
    static final FunctionType TYPE = new FunctionType(Identifiers.NAMESPACE, "user");

    @Persisted
    private final PersistedTable<String, Integer> userBasket =
        PersistedTable.of("basket", String.class, Integer.class);

    @Override
    public void invoke(Context context, Object input) {
        if (input instanceof ProtobufMessages.AddToCart) {
            ProtobufMessages.AddToCart addToCart = (ProtobufMessages.AddToCart) input;
            ProtobufMessages.RequestItem request =
                ProtobufMessages.RequestItem.newBuilder().setQuantity(addToCart.getQuantity()).build();
            Address address = new Address(Inventory.TYPE, addToCart.getItemId());
            context.send(address, request);
        }

        if (input instanceof ProtobufMessages.ItemAvailability) {
            ProtobufMessages.ItemAvailability availability = (ProtobufMessages.ItemAvailability) input;

            if (availability.getStatus() == ProtobufMessages.ItemAvailability.Status.INSTOCK) {
                userBasket.set(context.caller().id(), availability.getQuantity());
            }
            System.out.println(String.format("The current user basket for %s is:", context.self().id()));
            for (Map.Entry<String, Integer> entry : userBasket.entries()) {
                System.out.println(String.format("Item: %s Value: %d", entry.getKey(), entry.getValue()));
            }
            System.out.println("***************************************************************");

        }

        if (input instanceof ProtobufMessages.ClearCart) {
            for (Map.Entry<String, Integer> entry : userBasket.entries()) {
                ProtobufMessages.RestockItem item =
                    ProtobufMessages.RestockItem.newBuilder()
                        .setItemId(entry.getKey())
                        .setQuantity(entry.getValue())
                        .build();

                Address address = new Address(Inventory.TYPE, entry.getKey());
                context.send(address, item);
            }

            userBasket.clear();
            System.out.println(String.format("Cart cleared! The current user basket for %s is empty", context.self().id()));
            System.out.println("***************************************************************");


        }

        if (input instanceof ProtobufMessages.Checkout) {
            String intro = String.format("\nItems and their quantity checked out by %s:\n", context.self().id());
            String items =
                StreamSupport.stream(userBasket.entries().spliterator(), false)
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
            String decoration = "***************************************************************\n";
            String details = intro + items + decoration;
            ProtobufMessages.Receipt receipt =
                ProtobufMessages.Receipt.newBuilder()
                    .setUserId(context.self().id())
                    .setDetails(details)
                    .build();

            context.send(Identifiers.RECEIPT, receipt);
            userBasket.clear();
            System.out.println("User: Cart checked out! The current user basket is empty");
            System.out.println("***************************************************************");

        }
    }
}
