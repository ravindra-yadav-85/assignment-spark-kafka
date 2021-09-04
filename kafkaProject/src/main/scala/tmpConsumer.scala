import java.time.Duration
import java.util.{Collections, Properties}
import scala.collection.JavaConverters._
import com.codingharbour.protobuf.user.User
import io.confluent.kafka.serializers.protobuf.{KafkaProtobufDeserializer, KafkaProtobufDeserializerConfig}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer

object tmpConsumer extends App {

    val properties = new Properties
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "protobuf-consumer-group")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
//    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer")
  //ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
    properties.put("schema.registry.url", "http://localhost:8081")
    properties.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, classOf[User].getName)

    val consumer = new KafkaConsumer[String, User](properties)
    consumer.subscribe(Collections.singleton("assignment"))
    //poll the record from the topic
    while (true) {
      val records : ConsumerRecords[String, User] = consumer.poll(Duration.ofMillis(100))

      for (record <- asScalaIterator(records.iterator())) {
        println("name: " + record.value().name)
        println("favorite_number: " + record.value.favoriteNumber)
        println("favorite_color: " + record.value.favoriteColor)
      }
      consumer.commitAsync()
    }
}