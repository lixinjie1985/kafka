package org.eop.kafka.sample.producer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;

/**
 * @author lixinjie
 * @since 2017-07-07
 */
public class KfkProducer {

	public static void main(String[] args) throws Exception {
		//下面配置中的key名称可以从ProducerConfig和CommonClientConfigs中找出
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092，localhost:9093");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		//生产者是线程安全的，可以被多线程共享
		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		
		//下面五个参数中topic和value必须指定，其它三个可以不指定
		//其中partition表示要发送的分区，key也可以作为选择分区的依据（哈希值）
		//如果不指定上面两个和分区相关的参数的话，kafka将自己选择分区
		String topic = "topic";
		Integer partition = 0;
		Long timestamp = System.currentTimeMillis();
		String key = "key";
		String value = "value";
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, partition, timestamp, key, value);
		//异步发送记录，返回一个Future对象
		Future<RecordMetadata> fuRst = producer.send(record);
		//如果想要同步发送的效果的话，如下调用一下
		RecordMetadata rMt = fuRst.get();
		//记录在服务器端存储成功后，返回记录的元数据信息
		rMt.topic();
		rMt.partition();
		rMt.offset();
		rMt.timestamp();
		rMt.serializedKeySize();
		rMt.serializedValueSize();
		//异步带回调的发送，服务器端确认成功后执行回调
		fuRst = producer.send(record, new Callback(){
			//成功时得到结果，失败时得到异常，二者必有一个是null
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
			}
		});
		//使缓存记录立马发送，并阻塞直到服务器确认成功
		producer.flush();
		//根据topic获取它的分区，分区随着时间会变换的
		List<PartitionInfo> pInfos = producer.partitionsFor(topic);
		//一个分区信息
		PartitionInfo pInfo = pInfos.get(0);
		pInfo.topic();
		pInfo.partition();
		pInfo.leader();//leader节点
		pInfo.inSyncReplicas();//leader节点失败后，能够竞选成为leader的后备节点
		pInfo.replicas();//全部节点
		
		producer.close();
	}
}
