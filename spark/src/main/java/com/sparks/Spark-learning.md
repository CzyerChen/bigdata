> 结合工作中的部分问题和基础知识，进行总结学习

- 分布式计算
- spark master worker工作原理
- yarn工作原理
- client cluster的区别和原理
- spark 分区 partitions
- sparkstreaming kafka hbase/RDBMS
- sparkstreaming+kafka 限速
- 问题一个一个加，解读一点一点补


### 一、什么是分布式计算
### 二、spark master worker工作原理
### 三、yarn工作原理
### 四、client cluster的区别和原理
### 五、spark 分区 partitions
### 六、sparkstreaming kafka hbase/RDBMS

### 七、sparkstreaming+kafka 限速
#### 问题描述
sparkstreaming消费kafka的任务是持续不断的，那么如果数据流量一下子涌到kafka上，先前没有做好最大流量的计算和准备的话，很可能导致spark任务的阻塞，造成业务上的问题，这样的问题如何解决呢？

#### 解题思路
- 一个是前期设计的问题，这种情况本应该在设计的范畴之内，在kafka分区数，spark消费速率的设置
```text
--conf spark.streaming.kafka.maxRatePerPartition=100
```
1. 需要预算每天每秒大约有多少记录数，比如目前每10秒大约就是30000条，那么每秒就是3000条
2. 需要设计需要多少子任务的并i选哪个消费，进行topic分区数的设计，spark子任务数一般是topic分区数+1，一个子任务用于master
3. 对于每秒3000条数据，可以设置6个topic分区数，spark子任务7个，那么最大可消费的数量 为topic分区数*spark.streaming.kafka.maxRatePerPartition*60秒，这样我们当前情况最大消费量为6*100*60=36000，这是目前的最大量
4. 对于上面的最大量如果说不能满足初期要求的流量设计的，那么就是要调整参数了
5. 还可以开启动态executor 应对消费数据量的不均并达到较少的消耗：spark.streaming.dynamicAllocation.enabled=true,默认不开启

- 后期spark或者kafka的限速--spark限速/kafka限速

1.sparksteaming消费Kafka有两种方式，spark1.3之前只支持Receiver模式，之后提倡使用Direct模式，那么自然我也推荐新模式中提倡的Direct

2.spark限速
```text
Direct模式下设置:spark.streaming.kafka.maxRatePerPartition=100
receiver模式下设置:spark.streaming.receiver.maxRate=100
```
3.Kafka限速
```text
1）设置broker端参数quota.consumer.default
例如quota.consumer.default=15728640表示将连入该broker的所有consumer的TPS降到15MB/s以下，这个参数会全局生效，比较简单，可是无法单独限速

2）通过kafka-configs命令给consumer限速
bin/kafka-configs.sh  --zookeeper localhost:2181 --alter 
--add-config 'consumer_byte_rate=15728640'（每秒字节数） --entity-type clients --entity-name clientA
spark需要显示设置client.id,并且设置仅为每秒字节数，并不是控制具体条数
```


### 八、spark资源[动态分配](https://www.jianshu.com/p/79ebdb1dbaff)

