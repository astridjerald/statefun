package org.apache.flink.statefun.examples.shoppingcart;

import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.io.Router;

public class CheckoutRouter implements Router<ProtobufMessages.Checkout> {

    @Override
    public void route(ProtobufMessages.Checkout message, Downstream<ProtobufMessages.Checkout> downstream) {
        downstream.forward(Identifiers.USER, message.getUserId(), message);
    }
}
