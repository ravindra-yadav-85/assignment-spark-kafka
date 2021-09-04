import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.codingharbour.protobuf.SimpleMessageProtos.SimpleMessage;
import static com.codingharbour.protobuf.UserDetail.User;

public class KafkaConsumerApp {

    public static void main(String[] args) {
        KafkaConsumerApp protobufConsumer = new KafkaConsumerApp();
        protobufConsumer.readMessages();
    }

    public void readMessages() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "protobuf-consumer-group");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);

        properties.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        properties.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, User.class.getName());

        KafkaConsumer<String, User> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Collections.singleton("assignment"));

        //poll the record from the topic
        while (true) {
            ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, User> record : records) {
//                System.out.println("Message content: " + record.value().getContent());
//                System.out.println("Message time: " + record.value().getDateTime());
                System.out.println("name: " + record.value().getName());
                System.out.println("favorite_number: " + record.value().getFavoriteNumber());
                System.out.println("favorite_color: " + record.value().getFavoriteColor());
            }
            consumer.commitAsync();
        }
    }
}