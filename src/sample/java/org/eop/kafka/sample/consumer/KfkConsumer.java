package org.eop.kafka.sample.consumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

/**
 * @author lixinjie
 * @since 2017-07-07
 */
public class KfkConsumer {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		//下面配置中的key名称可以从ConsumerConfig和CommonClientConfigs中找到
		Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092,localhost:9093");
	    props.put("group.id", "test");
	    props.put("enable.auto.commit", "true");
	    props.put("auto.commit.interval.ms", "1000");
	    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    
	    //消费者是非线程安全的，只能用于一个线程
	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	    
	    //获取分配给当前消费者的分区
	    Set<TopicPartition> tps = consumer.assignment();
	    //获取当前消费者的订阅topic
	    Set<String> subs = consumer.subscription();
	    //订阅topic，当分配给消费者的分区发生变化时，回调会被执行
	    consumer.subscribe(Arrays.asList("topic1", "topic2"), new ConsumerRebalanceListener() {

			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
			}

			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
			}
			
	    });
	    //订阅topic
	    consumer.subscribe(Arrays.asList("foo", "bar"));
	    //订阅topic，按正则表达式订阅
	    consumer.subscribe(Pattern.compile(""), new ConsumerRebalanceListener(){

			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
			}

			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
			}
	    	
	    });
	    //清除所有topic订阅，也会清除手动分配给当前消费者的分区
	    consumer.unsubscribe();
	    //手动给当前消费者分配分区
	    consumer.assign(Arrays.asList(new TopicPartition("topic", 0), new TopicPartition("topic", 1)));
	    //从服务器拉取记录，实际应放到while循环中反复拉取
	    long timeout = Long.MAX_VALUE;
	    ConsumerRecords<String, String> cRecs = consumer.poll(timeout);
	    //拉取记录的结果，里面存储了每个分区和对应的记录列表的映射关系
	    //Map<TopicPartition, List<ConsumerRecord<K, V>>> records
	    //获取单个分区的所有记录
	    List<ConsumerRecord<String, String>> crecs = cRecs.records(new TopicPartition("topic", 0));
	    //根据topic获取所有记录
	    String topic = "topic";
	    Iterable<ConsumerRecord<String, String>> icrec = cRecs.records(topic);
	    //获得所有分区
	    Set<TopicPartition> ps = cRecs.partitions();
	    //获得所有记录
	    Iterator<ConsumerRecord<String, String>> irec = cRecs.iterator();
	    //单个记录
	    ConsumerRecord<String, String> crec = crecs.get(0);
	    crec.topic();
	    crec.partition();
	    crec.offset();
	    crec.timestamp();
	    crec.key();
	    crec.value();
	    crec.serializedKeySize();
	    crec.serializedValueSize();
	    //同步提交最后一次调用完poll方法后的
	    //所有分区记录的偏移量给kafka，并阻塞直到完成
	    consumer.commitSync();
	    //同步提交自己设置的分区偏移量给kafka
	    consumer.commitSync((Map<TopicPartition, OffsetAndMetadata>)null);
	    //异步提交最后一次调用完poll方法后的
	    //所有分区记录的偏移量给kafka
	    consumer.commitAsync();
	    //异步提交最后一次调用完poll方法后的
	    //所有分区记录的偏移量给kafka，带有回调
	    //服务器执行成功后，回调会被执行
	    consumer.commitAsync(new OffsetCommitCallback() {
	    	//成功时异常为null
			@Override
			public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
			}
	    	
	    });
	    //异步提交自己设置的分区偏移量给kafka，带有回调
	    //服务器执行成功后，回调会被执行
	    consumer.commitAsync((Map<TopicPartition, OffsetAndMetadata>)null, new OffsetCommitCallback() {
	    	//成功时异常为null
			@Override
			public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
			}
	    	
	    });
	    //设置一个分区的偏移量
	    long offset = 0L;
	    consumer.seek(new TopicPartition(topic, 0), offset);
	    //把指定分区的偏移量设置到开始
	    consumer.seekToBeginning(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 1)));
	    //把指定分区的偏移量设置到最后
	    consumer.seekToEnd(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 1)));
	    //获得一个分区的下一条记录偏移量
	    offset = consumer.position(new TopicPartition("topic", 0));
	    //获取一个分区的最后一次提交的偏移量
	    OffsetAndMetadata offmeta = consumer.committed(new TopicPartition("topic", 0));
	    //获取一个topic的分区
	    List<PartitionInfo> pInfos = consumer.partitionsFor(topic);
	    //获取用户有权看到topic和对应的分区
	    Map<String, List<PartitionInfo>> pMaps = consumer.listTopics();
	    //暂停从指定分区拉取记录
	    consumer.pause(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 0)));
	    //恢复从指定分区拉取记录
	    consumer.resume(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 0)));
	    //获取暂停的分区
	    Set<TopicPartition> tps1 = consumer.paused();
	    //根据分区时间戳，查找偏移量
	    Map<TopicPartition, OffsetAndTimestamp> tpot = consumer.offsetsForTimes((Map<TopicPartition, Long>)null);
	    //获得分区对应的第一个偏移量
	    Map<TopicPartition, Long> tpm = consumer.beginningOffsets(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 0)));
	    //获得分区对应的最后一个偏移量
	    Map<TopicPartition, Long> tpm1 = consumer.endOffsets(Arrays.asList(new TopicPartition("topic1", 0), new TopicPartition("topic2", 0)));
	    //唤醒消费者，此方法线程安全，可以在另一个线程中调用，用于中止一个长轮询
	    //当线程正在阻塞时会抛出WakeupException异常
	    consumer.wakeup();
	    
	    consumer.close();
	}

}
