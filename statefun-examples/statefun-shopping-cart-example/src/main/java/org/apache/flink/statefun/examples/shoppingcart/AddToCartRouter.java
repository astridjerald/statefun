package org.apache.flink.statefun.examples.shoppingcart;

import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.io.Router;

public class AddToCartRouter implements Router<ProtobufMessages.AddToCart> {

    @Override
    public void route(ProtobufMessages.AddToCart message, Downstream<ProtobufMessages.AddToCart> downstream) {
        downstream.forward(UserShoppingCart.TYPE, message.getUserId(), message);
    }
}
