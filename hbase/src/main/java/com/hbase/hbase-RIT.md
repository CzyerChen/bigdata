- hbase的基本原理学习日后整理，由于在测试环境中使用遇到了RIT的问题，今天在这里总结一下

### 什么时RIT
- HBase 永久RIT(Region-In-Transition)问题：异常关机导致HBase表损坏和丢失，大量Regions 处于Offline状态，无法上线

### 问题的定位
- 能够从日志里面看到的表象问题有：
```text
Initializing Master file system (since 10mins, 16sec ago) 
The RegionServer is initializing!
```
```text
大量的[AM.ZK.Worker-pool5-t1] master.RegionStates: Transition:{xxxxxxxxxx state}
[AM.ZK.Worker-pool5-t1] master.RegionStates: Offlined:xxxxxxxxxx from slave xxxxxxx:xxxx
```
- 这种情况，我们首先要确保hdfs不在保护模式下
```text
hadoop dfsadmin -safemode get
```
如果显示Safe mode is OFF ，那就是没有问题啦，如果是ON呢就要把它关闭hadoop dfsadmin -safemode leave

- 从HBASE的WEB UI上也能看到，User Tables出能够看到很多Offline Regions
- 如果以上还不能帮助你定位问题的话，就可以从根本的hdfs上来看看
- 查看hdfs的状态报告
```text
hadoop dfsadmin -report

Configured Capacity: 321886154752 (299.78 GB)
Present Capacity: 247437848576 (230.44 GB)
DFS Remaining: 171817480192 (160.02 GB)
DFS Used: 75620368384 (70.43 GB)
DFS Used%: 30.56%
Under replicated blocks: 0
Blocks with corrupt replicas: 0
Missing blocks: 0
Missing blocks (with replication factor 1): 0

-------------------------------------------------
Live datanodes (1):

Name: 192.168.35.129:50010 (hynode1.hde.h3c.com)
Hostname: hynode1.hde.h3c.com
Decommission Status : Normal
Configured Capacity: 321886154752 (299.78 GB)
DFS Used: 75620368384 (70.43 GB)
Non DFS Used: 58090573824 (54.10 GB)
DFS Remaining: 171817480192 (160.02 GB)
DFS Used%: 23.49%
DFS Remaining%: 53.38%
Configured Cache Capacity: 0 (0 B)
Cache Used: 0 (0 B)
Cache Remaining: 0 (0 B)
Cache Used%: 100.00%
Cache Remaining%: 0.00%
Xceivers: 1
Last contact: Fri Mar 22 15:08:02 CST 2019

```
- 因为测试机器没有这个问题，如果是出问题的机器，就要看Under replicated blocks和Missing blocks这个指标，会有数值
```text
Under replicated blocks　　　　  副本数少于指定副本数的block数量
Blocks with corrupt replicas　　 存在损坏副本的block的数据
Missing blocks　　　　　　　　    丢失block数量
```
- 查看损坏文件、当前hdfs的副本数
```text
hdfs fsck /  或者   hadoop fsck -locations

 Status: HEALTHY
 Total size:	75025783486 B
 Total dirs:	916
 Total files:	969
 Total symlinks:		0
 Total blocks (validated):	1261 (avg. block size 59497052 B)
 Minimally replicated blocks:	1261 (100.0 %)
 Over-replicated blocks:	0 (0.0 %)
 Under-replicated blocks:	0 (0.0 %)
 Mis-replicated blocks:		0 (0.0 %)
 Default replication factor:	1
 Average block replication:	1.0
 Corrupt blocks:		0
 Missing replicas:		0 (0.0 %)
 Number of data-nodes:		1
 Number of racks:		1
FSCK ended at Fri Mar 22 15:10:47 CST 2019 in 216 milliseconds

```
- 本人当时遇到的问题就是存在副本没有完全复制完毕的问题，可能是一些中断或损坏，使得Hadoop里存在未修复的block，因而出现HBase Offline Regions 和RIT(Region-In-Transition)问题


### 恢复步骤
- 由于以上本人遇到的是RIT问题，因而恢复步骤如下
- 修复Missing blocks
```text
hadoop fs -setrep -R 3 /

这个命令可以手动的将只存有一个副本或者是两个副本的block ,重新生成3个，从而找回丢失的副本
```
- 通过以上步骤，如果不存在损坏严重的block那就可以恢复好了，再通过hadoop dfsadmin -report查看Missing block
- 如果还有丢失的block，那就需要删除了
```text
hdfs fsck -delete //删除损坏文件
```
- 经过以上步骤后，重启Hadoop 重启hbase ,看问题是否解决


###  其他检测功能
- hbase hbck  hbase自身的检测工具
集群节点的基本信息
```text
..
Number of live region servers: 1
Number of dead region servers: 0
Master: hostname,16000,1553240010393
Number of backup masters: 0
Average load: 97.0
Number of requests: 0
Number of regions: 97
Number of regions in transition: 0

Number of empty REGIONINFO_QUALIFIER rows in hbase:meta: 0
Number of Tables: 22
....
```
```text
Summary:
Table pentaho_mappings is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table replication_source_table is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table test_gis1 is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table test_gis is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table sg_his:sg_con_pwrgrid_h5_mea is okay.
    Number of regions: 2
    Deployed on:  hostname,16020,1553240010685
Table hbase:meta is okay.
    Number of regions: 1
    Deployed on: hostname,16020,1553240010685
Table test_import is okay.
    Number of regions: 1
    Deployed on: hostname,16020,1553240010685
Table SYSTEM.CATALOG is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table TEST is okay.
    Number of regions: 16
    Deployed on: hostname,16020,1553240010685
Table test is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table sg_his:sg_con_plant_h5_mea is okay.
    Number of regions: 2
    Deployed on:  hostname,16020,1553240010685
Table test1 is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table test2 is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table test3 is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table test4 is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table sg_his:sg_dev_busbar_h5_mea is okay.
    Number of regions: 25
    Deployed on:  hostname,16020,1553240010685
Table hbase:namespace is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table TextHbase is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table SYSTEM.SEQUENCE is okay.
    Number of regions: 1
    Deployed on: hostname,16020,1553240010685
Table SYSTEM.LOG is okay.
    Number of regions: 32
    Deployed on:  hostname,16020,1553240010685
Table SYSTEM.FUNCTION is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table plant_test is okay.
    Number of regions: 2
    Deployed on:  hostname,16020,1553240010685
Table SYSTEM.MUTEX is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
Table SYSTEM.STATS is okay.
    Number of regions: 1
    Deployed on:  hostname,16020,1553240010685
0 inconsistencies detected.
Status: OK
```
最后的检测结果就是ok啦，如果出现问题就可以hbase hbck -repair，这个命令修复
- 以上是因为我的集群没有问题，如果有RIT问题，可能出现以下ERROR：
```text
ERROR: Region { meta => test,02,1517807389209.3eb41df715cdd0f9a2b0ce6550b586b3., hdfs => hdfs://hostname:8090/hbase/data/default/test/3eb41df715cdd0f9a2b0ce6550b586b3, deployed => , replicaId => 0 } not deployed on any region server.
18/03/19 11:40:08 INFO util.HBaseFsck: Handling overlap merges in parallel. set hbasefsck.overlap.merge.parallel to false to run serially.
ERROR: There is a hole in the region chain between 02 and 03.  You need to create a new .regioninfo and region dir in hdfs to plug the hole.
ERROR: Found inconsistency in table test

```
- 可以根据检测报告看是什么样的问题，用下面这些修复命令
- hbase hbck -repair 
- 修复 .META表 ：hbase hbck -fixMeta
- 修复漏洞  hbase hbck -fixHdfsHoles 
- 修复引用文件  hbase hbck -fixReferenceFiles 
- 修复assignments  hbase hbck -fixAssignments,用于修复未分配，错误分配或者多次分配Region的问题
