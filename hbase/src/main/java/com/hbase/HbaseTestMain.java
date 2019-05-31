package com.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HbaseTestMain {

    public static Configuration configuration;

    static {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "10.10.10.66");
        configuration.set("hbase.master", "10.10.10.66");
    }

    public static void main(String[] args) {
        //createTable("profile");
        //insertData("profile");
        //QueryAll("profile");
        QueryAll("test");
        //QueryByCondition1("test");
        //QueryByCondition2("test");
        //QueryByCondition3("test");
        //deleteRow("wujintao","abcdef");
        // deleteByCondition("wujintao","abcdef");
    }


    public static void createTable(String tableName) {
        System.out.println("start create table ……");
        try {
            HBaseAdmin admin = new HBaseAdmin(configuration);
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
                System.out.println(tableName + " is exist,detele….");
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
            tableDescriptor.addFamily(new HColumnDescriptor("name"));
            tableDescriptor.addFamily(new HColumnDescriptor("job"));
            tableDescriptor.addFamily(new HColumnDescriptor("contact"));
            tableDescriptor.addFamily(new HColumnDescriptor("others"));
            admin.createTable(tableDescriptor);
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void insertData(String tableName) {
        System.out.println("start insert data ……");
        HTablePool pool = new HTablePool(configuration, 1000);

        //HTable table = (HTable) pool.getTable(tableName);
        Put put = new Put("112233bbbcccc".getBytes());
        put.add("name".getBytes(), null, "test".getBytes());
        put.add("job".getBytes(), null, "software engineer".getBytes());
        put.add("contact".getBytes(), null, "1377600000000".getBytes());
        put.add("others".getBytes(), null, "nothing".getBytes());
        try {
            pool.getTable(tableName).put(put);
            /* table.put(put);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("end insert data ……");
    }


    public static void dropTable(String tableName) {
        try {
            HBaseAdmin admin = new HBaseAdmin(configuration);
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void deleteRow(String tablename, String rowkey) {
        try {
            HTable table = new HTable(configuration, tablename);
            List list = new ArrayList<>();
            Delete d1 = new Delete(rowkey.getBytes());
            list.add(d1);

            table.delete(list);
            System.out.println("?????!");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void QueryAll(String tableName) {
        HTablePool pool = new HTablePool(configuration, 1000);
        //HTable table = (HTable) pool.getTable(tableName);

        try {
            ResultScanner rs = pool.getTable(tableName).getScanner(new Scan());
            for (Result r : rs) {
                System.out.println("???rowkey:" + new String(r.getRow()));
                for (KeyValue keyValue : r.raw()) {
                    System.out.println("??" + new String(keyValue.getFamily())
                            + "====?:" + new String(keyValue.getValue()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void QueryByCondition1(String tableName) {

        HTablePool pool = new HTablePool(configuration, 1000);
        /*HTable table = (HTable) pool.getTable(tableName);*/
        try {
            Get scan = new Get("112233bbbcccc".getBytes());// ??rowkey??
            Result r = pool.getTable(tableName).get(scan);
            if (r != null && r.getRow() != null) {
                System.out.println("???rowkey:" + new String(r.getRow()));
                for (KeyValue keyValue : r.raw()) {
                    System.out.println("??" + new String(keyValue.getFamily())
                            + "====?:" + new String(keyValue.getValue()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void QueryByCondition2(String tableName) {

        try {
            HTablePool pool = new HTablePool(configuration, 1000);
            /*HTable table = (HTable) pool.getTable(tableName);*/
            Filter filter = new SingleColumnValueFilter(Bytes
                    .toBytes("column1"), null, CompareFilter.CompareOp.EQUAL, Bytes
                    .toBytes("aaa1"));
            Scan s = new Scan();
            s.setFilter(filter);
            ResultScanner rs = pool.getTable(tableName).getScanner(s);
            for (Result r : rs) {
                System.out.println("???rowkey:" + new String(r.getRow()));
                for (KeyValue keyValue : r.raw()) {
                    System.out.println("??" + new String(keyValue.getFamily())
                            + "====?:" + new String(keyValue.getValue()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void QueryByCondition3(String tableName) {

        try {
            HTablePool pool = new HTablePool(configuration, 1000);
            /* HTable table = (HTable) pool.getTable(tableName);*/

            List<Filter> filters = new ArrayList<Filter>();

            Filter filter1 = new SingleColumnValueFilter(Bytes
                    .toBytes("column1"), null, CompareFilter.CompareOp.EQUAL, Bytes
                    .toBytes("aaa1"));
            filters.add(filter1);

            Filter filter2 = new SingleColumnValueFilter(Bytes
                    .toBytes("column2"), null, CompareFilter.CompareOp.EQUAL, Bytes
                    .toBytes("bbb1"));
            filters.add(filter2);

            Filter filter3 = new SingleColumnValueFilter(Bytes
                    .toBytes("column3"), null, CompareFilter.CompareOp.EQUAL, Bytes
                    .toBytes("ccc1"));
            filters.add(filter3);

            FilterList filterList1 = new FilterList(filters);

            Scan scan = new Scan();
            scan.setFilter(filterList1);
            ResultScanner rs = pool.getTable(tableName).getScanner(scan);

            for (Result r : rs) {
                if (r != null && r.getRow() != null) {
                    System.out.println("???rowkey:" + new String(r.getRow()));
                    for (KeyValue keyValue : r.raw()) {
                        System.out.println("??" + new String(keyValue.getFamily())
                                + "====?:" + new String(keyValue.getValue()));
                    }
                }
            }
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
