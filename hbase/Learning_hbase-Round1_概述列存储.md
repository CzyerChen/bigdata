### 行存储 和 列存储
- 行存储，比如一个人有（ID，姓名，性别，年龄）三个字段，一条数据，就是就是一个（1，‘xxx’,F,13）
- 列式存储情况，以上有四列，那这就是4行不同的记录，(1),(XXXX),(F),(13)
- 行倾向于结构稳定的Java Bean类型的数据，列倾向于按需添加的Map的弱结构化数据，列式存储还需要存储列名

### hbase的概述
#### 数据库主要有两种：
1、RDBMS ：关系型数据库 
— Oracle(10亿级别的数据就会卡顿) 
— Mysql（亿级别的数据就会卡顿） 
— SqlServer
 
2、Nosql :非关系型数据库 
— Hbase 
— MongoDB 
— redis

#### 背景
主要是实时性查询、大数据量的数据

- 数据采集
    - sqoop(Flume)
    - kettle(ETL tools)
    - program(JDBC JAVAPI)

- 存储
    - 分布式主从架构 
    - 底层存储依赖于hadoop HDFS

- 特殊概念 ——-HBase的表
    - 列簇 ——>是表的schema，创建表的时候至少有一个列簇 
    - 用相似属性的列的集合 ，Hbase中每一列必须属于某一个列簇 

以上说明了传统关系型数据和HBase当中的区别，在HBASE里面访问姓名的时候一定要加上列簇，基本信息：姓名 =====》 列簇：列名称（列标签）

#### 列存储 
- rdbms: 数据都根据表存储 
    - 如果有一列没有值，那就是null 
- nosql :数据按照键值存储 
    - 如果有一列没有值，那就没有这一列
- versions:多版本存储 
- rdbms：行和列唯一确定一个单元格，单元格中只存储一个值 
- hbase： 行和列簇+列标签唯一确定一个单元格（组），单元格可以存储多个值 
    — 个数可以自定义，默认值为1，不过可以设置
    — 这个单元格组按照时间戳（timestamp）区分
    — 默认检索出最新插入的值
    — 底层存储是字节数据，没有int double的概念 

- rowkey 
    - 和rdbms中的id是一致的，唯一标识一行数据的标识 
    — hbase中默认检索，读写都是通过rowkey进行的 
    — 按照字典顺序进行排列 
    — rowkey + 列簇+ 列标签 唯一标识一个单元格组 ，格组里面的不同数据依靠timestamp区分

- mysql和hbase的表结构对比
 
 
#### hbase的实际存储的情况（注意：列存储） 
- 里面所有的存储都是按照字典顺序排列的，首先按照rowkey ,其次按照列簇，最后按照列名，rowkey可以是数字也可以字符，底层都是字节数据 
```text
001 basic:age ts 18 
001 basic:name ts zhangsan 
001 basic:sex ts male 
001 contact:phone ts 110 
002 basic:age ts 20 
002 basic:name ts lisi 
002 contact:phone ts 120
```
