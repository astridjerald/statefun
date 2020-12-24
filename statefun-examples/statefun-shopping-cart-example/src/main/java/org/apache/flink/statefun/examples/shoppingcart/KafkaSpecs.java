package org.apache.flink.statefun.examples.shoppingcart;

import org.apache.flink.statefun.examples.shoppingcart.generated.ProtobufMessages;
import org.apache.flink.statefun.sdk.io.EgressSpec;
import org.apache.flink.statefun.sdk.io.IngressSpec;
import org.apache.flink.statefun.sdk.kafka.KafkaEgressBuilder;
import org.apache.flink.statefun.sdk.kafka.KafkaEgressSerializer;
import org.apache.flink.statefun.sdk.kafka.KafkaIngressBuilder;
import org.apache.flink.statefun.sdk.kafka.KafkaIngressDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

final class KafkaSpecs {
    private static final String KAFKA_SERVER = "kafka-broker:9092";
    private static final String ADD_TO_CART_KAFKA_TOPIC_NAME = "add-to-cart";
    private static final String CLEAR_CART_KAFKA_TOPIC_NAME = "clear-cart";
    private static final String CHECKOUT_KAFKA_TOPIC_NAME = "checkout";
    private static final String RECEIPT_KAFKA_TOPIC_NAME = "receipt";
    private static final String RESTOCK_ITEM_KAFKA_TOPIC_NAME = "restock-item";

    static IngressSpec<ProtobufMessages.AddToCart> ADD_TO_CART_SPEC =
        KafkaIngressBuilder.forIdentifier(Identifiers.ADD_TO_CART)
            .withKafkaAddress(KAFKA_SERVER)
            .withTopic(ADD_TO_CART_KAFKA_TOPIC_NAME)
            .withProperty(ConsumerConfig.GROUP_ID_CONFIG, "statefun-add-to-cart-group")
            .withDeserializer(AddToCartDeserializer.class)
            .build();

    static IngressSpec<ProtobufMessages.ClearCart> CLEAR_CART_SPEC =
        KafkaIngressBuilder.forIdentifier(Identifiers.CLEAR_CART)
            .withKafkaAddress(KAFKA_SERVER)
            .withTopic(CLEAR_CART_KAFKA_TOPIC_NAME)
            .withProperty(ConsumerConfig.GROUP_ID_CONFIG, "statefun-clear-cart-group")
            .withDeserializer(ClearCartDeserializer.class)
            .build();

    static IngressSpec<ProtobufMessages.Checkout> CHECKOUT_SPEC =
        KafkaIngressBuilder.forIdentifier(Identifiers.CHECKOUT)
            .withKafkaAddress(KAFKA_SERVER)
            .withTopic(CHECKOUT_KAFKA_TOPIC_NAME)
            .withProperty(ConsumerConfig.GROUP_ID_CONFIG, "statefun-checkout-group")
            .withDeserializer(CheckoutDeserializer.class)
            .build();

    static IngressSpec<ProtobufMessages.RestockItem> RESTOCK_ITEM_SPEC =
        KafkaIngressBuilder.forIdentifier(Identifiers.RESTOCK_ITEM)
            .withKafkaAddress(KAFKA_SERVER)
            .withTopic(RESTOCK_ITEM_KAFKA_TOPIC_NAME)
            .withProperty(ConsumerConfig.GROUP_ID_CONFIG, "statefun-restock-item-group")
            .withDeserializer(RestockItemDeserializer.class)
        .build();

    static EgressSpec<ProtobufMessages.Receipt> RECEIPT_SPEC =
        KafkaEgressBuilder.forIdentifier(Identifiers.RECEIPT)
            .withKafkaAddress(KAFKA_SERVER)
            .withSerializer(ReceiptSerializer.class)
        .build();

    private static final class AddToCartDeserializer
        implements KafkaIngressDeserializer<ProtobufMessages.AddToCart> {

        private static final long serialVersionUID = 1L;

        @Override
        public ProtobufMessages.AddToCart deserialize(ConsumerRecord<byte[], byte[]> input) {
            String[] data = new String(input.value(), StandardCharsets.UTF_8).split(" ");
            String userId = data[0];
            String itemId = data[1];
            int quantity = Integer.parseInt(data[2]);
            return ProtobufMessages.AddToCart.newBuilder().setUserId(userId).setItemId(itemId).setQuantity(quantity).build();
        }
    }

    private static final class ClearCartDeserializer
        implements KafkaIngressDeserializer<ProtobufMessages.ClearCart> {

        private static final long serialVersionUID = 1L;

        @Override
        public ProtobufMessages.ClearCart deserialize(ConsumerRecord<byte[], byte[]> input) {
            String[] data = new String(input.value(), StandardCharsets.UTF_8).split(" ");
            String userId = data[0];

            return ProtobufMessages.ClearCart.newBuilder().setUserId(userId).build();
        }
    }

    private static final class CheckoutDeserializer
        implements KafkaIngressDeserializer<ProtobufMessages.Checkout> {

        private static final long serialVersionUID = 1L;

        @Override
        public ProtobufMessages.Checkout deserialize(ConsumerRecord<byte[], byte[]> input) {
            String[] data = new String(input.value(), StandardCharsets.UTF_8).split(" ");
            String userId = data[0];

            return ProtobufMessages.Checkout.newBuilder().setUserId(userId).build();
        }
    }

    private static final class RestockItemDeserializer
        implements KafkaIngressDeserializer<ProtobufMessages.RestockItem> {

        private static final long serialVersionUID = 1L;

        @Override
        public ProtobufMessages.RestockItem deserialize(ConsumerRecord<byte[], byte[]> input) {
            String[] data = new String(input.value(), StandardCharsets.UTF_8).split(" ");
            String itemId = data[1];
            int quantity = Integer.parseInt(data[2]);
            return ProtobufMessages.RestockItem.newBuilder().setItemId(itemId).setQuantity(quantity).build();
        }
        }

    private static final class ReceiptSerializer
        implements KafkaEgressSerializer<ProtobufMessages.Receipt> {
        private static final long serialVersionUID = 1L;

        @Override
        public ProducerRecord<byte[], byte[]> serialize(ProtobufMessages.Receipt receipt) {
            byte[] key = receipt.getUserId().getBytes(StandardCharsets.UTF_8);
            byte[] value = receipt.getDetails().getBytes(StandardCharsets.UTF_8);

            return new ProducerRecord<>(RECEIPT_KAFKA_TOPIC_NAME, key, value);
        }
    }
}
