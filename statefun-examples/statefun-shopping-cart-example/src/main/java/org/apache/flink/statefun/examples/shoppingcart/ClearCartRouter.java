package org.apache.flink.statefun.examples.shoppingcart;

import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.io.Router;

public class ClearCartRouter implements Router<ProtobufMessages.ClearCart> {

    @Override
    public void route(ProtobufMessages.ClearCart message, Downstream<ProtobufMessages.ClearCart> downstream) {
        downstream.forward(UserShoppingCart.TYPE, message.getUserId(), message);
    }
}