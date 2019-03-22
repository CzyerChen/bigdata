- zookeeper的安装
     - 解压压缩包
     - 配置zoo.cfg
     - 启动zookeeper /bin/zkServer.sh start

- 通过以上步骤，就可以启动一个zookeeper分布式协调服务了，通过jps能够看到QuorumPeerMain的zookeeper进程
- 下面是想简单介绍一下，zookeeper那些自带的命令，zookeeper能够接收注册信息，能够给节点内信息发送消息，能够做failover，能够做负载均衡，那我们就通过它自带的命令看看它上面存储了什么

### 一、zookeeper数据存储
- zookeeper数据存储比较简单，就是类似于hadoop内部的，也和linux的目录类似，有节点路径、节点信息、ACL等
- 以上目录结构，可以通过进入zookeeper-shell 查看，/bin/zkCli.sh 
- 进入zookeeper存储系统之后，也可以通过help命令查看帮助
```text
ls:查看当前 ZooKeeper 中所包含的内容          对应linux下的ls
 
ls2:查看当前节点数据并能看到更新次数等数据      对应Linux下的ls -l

create:创建一个新的 znode                    对应Linux下的mkdir

get:获取znode数据信息                        
 
rmr:删除该节点                               对应Linux下的rm / rm -rf
```
### 二、节点ACL信息
- 描述了节点的访问控制列表
- ZK的节点有5种操作权限：CREATE、READ、WRITE、DELETE、ADMIN 也就是 增、删、改、查、管理权限，这5种权限简写为crwda
- 身份的认证有4种方式：
```text
world，anyone：默认方式，相当于全世界都能访问
auth：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
digest：即用户名:密码这种方式认证，这也是业务系统中最常用的
ip：使用Ip地址认证
```
- 不配置的权限的情况下，我随意查看一个列表的权限可以看到以下情况
```text
'world',anyone
: cdrwa
```
- 如果我们要做一次创建于赋权的话，可以走以下流程：
```text
1. 创建测试目录
create /testfile 'test-file'
Created /testfile

2. 设置用户
addauth digest claire:12345

3. 设置只读权限
setAcl /testfile auth:claire:12345:r

cZxid = 0x2
ctime = Fri Mar 22 14:37:06 CST 2019
mZxid = 0x2
mtime = Fri Mar 22 14:37:06 CST 2019
pZxid = 0x2
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 11
numChildren = 0

4. 获取权限信息
getAcl /testfile

'digest,'claire:koq6n2Jxxy5oswdpXhr02TaNpqk=
: r

5. 获取测试
get /testfile

'test-file'
cZxid = 0x2
ctime = Fri Mar 22 14:37:06 CST 2019
mZxid = 0x2
mtime = Fri Mar 22 14:37:06 CST 2019
pZxid = 0x2
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 11
numChildren = 0

```

### 三、查看zookeeper节点目录
- ls2 /zookeeper
- 我们以查看以下hbase 中的信息为例，可以了解到这个zk数据存储
```text
ls2 /hbase

[replication, meta-region-server, rs, splitWAL, backup-masters, table-lock, flush-table-proc, region-in-transition, online-snapshot, switch, master, running, recovering-regions, draining, namespace, hbaseid, table]
cZxid = 0x100000002
ctime = Mon Nov 27 18:08:47 CST 2017
mZxid = 0x100000002
mtime = Mon Nov 27 18:08:47 CST 2017
pZxid = 0x200000430
cversion = 49
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 17
```
- 查看hbase集群在zookeeper记录的信息
```text
[slave-2,16020,1511853261863, slave-1,16020,1511853261718]
cZxid = 0x100000004
ctime = Fri Mar 15 18:08:47 CST 2019
mZxid = 0x100000004
mtime = Fri Mar 15 18:08:47 CST 2019
pZxid = 0x200000426
cversion = 22
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 2

```
- 查看所有表
```text
[zk: localhost:2181(CONNECTED) 11] ls2 /hbase/table
[aa,hbase:meta, hbase:namespace]
cZxid = 0x100000006
ctime = Fri Mar 15 18:08:47 CST 2019
mZxid = 0x100000006
mtime = Fri Mar 15 18:08:47 CST 2019
pZxid = 0x200000478
cversion = 56
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 24
```
- 可以从zookeeper目录上看到hbase中的所有表，hbase从节点的信息和状态等

