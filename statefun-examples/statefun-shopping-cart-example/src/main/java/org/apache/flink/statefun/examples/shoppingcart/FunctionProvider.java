package org.apache.flink.statefun.examples.shoppingcart;

import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.StatefulFunction;
import org.apache.flink.statefun.sdk.StatefulFunctionProvider;

public class FunctionProvider implements StatefulFunctionProvider {
    @Override
    public StatefulFunction functionOfType(FunctionType functionType) {
        if (functionType.equals(Inventory.TYPE)){
            return new Inventory();
        }
        else if (functionType.equals(UserShoppingCart.TYPE)){
            return new UserShoppingCart();
        }
        else {
            throw new IllegalArgumentException("Unknown type " + functionType);
        }
    }
}
