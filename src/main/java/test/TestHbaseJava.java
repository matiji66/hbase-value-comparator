package test;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.pateo.bean.User;
import com.pateo.bean.util.ParserUtils;
import com.pateo.hbase.defined.comparator.CustomNumberComparator;

/**
 *  自定义比较强参考实现
 *   *  
 *  列值为'', ' ' '+' 等会导致filter失败，应该是数值转换失败导致的
 *  解决方案，在value comparator之前加上几个简单的过滤器，过滤掉这些脏数据就行了
 *  用的32位protoc 2.5 编译出来的java文件，可能导致兼容性问题，类型只支持double类型进行过滤
 *     
 * @author matiji66 20171226
 * 
 */
public class TestHbaseJava {

	static final byte[] family = Bytes.toBytes("f1");
	static final byte[] col_name = Bytes.toBytes("name");
	static final byte[] col_age = Bytes.toBytes("age");

	public static void main(String args[]) throws IOException {

		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "172.16.5.30");
		conf.set("hbase.zookeeper.property.clientPort", "2181");

		// Connection 的创建是个重量级的工作，线程安全，是操作hbase的入口
		Connection conn = ConnectionFactory.createConnection(conf);
		Admin admin = conn.getAdmin();
		// 使用Admin创建和删除表
		TableName userTable = TableName.valueOf("user");
		createTable(admin, userTable);

		// 插入、查询、扫描、删除操作
		// 获取 user 表
		Table table = conn.getTable(userTable);

		inserData(table);

		// 查询某条数据
		getTest(table);

		// 扫描数据
		System.out.println("====scan start ======");
		Scan s = new Scan();
		
		FilterList filterList = new FilterList(); 
		//1. 过滤 "" ,否则会出错
		SingleColumnValueFilter nullFilter = new SingleColumnValueFilter( 
		family, 
		col_age, 
		CompareFilter.CompareOp.NOT_EQUAL, 
		new BinaryComparator(Bytes.toBytes("")) 
		);
		filterList.addFilter(nullFilter);
		
		//2. 过滤 "*-+；：" 等脏数据
		SingleColumnValueFilter ileague_0 = new SingleColumnValueFilter( 
		family, 
		col_age, 
		CompareFilter.CompareOp.GREATER_OR_EQUAL, 
		new BinaryComparator(Bytes.toBytes("0")) 
		);		
		filterList.addFilter(ileague_0);
		SingleColumnValueFilter ileague_9 = new SingleColumnValueFilter( 
				family, 
				col_age, 
				CompareFilter.CompareOp.LESS_OR_EQUAL, 
				new BinaryComparator(Bytes.toBytes("9")) 
				);		
		filterList.addFilter(ileague_9);
		
		//3. 真正的value filter 开始了
		// 过滤出年龄大于 5 小于80 的user 
		SingleColumnValueFilter low_bound = new SingleColumnValueFilter(  
	            family,   
	            col_age,   
	            CompareFilter.CompareOp.GREATER,   
	            new CustomNumberComparator(Bytes.toBytes(5.),"double" ) // 18.0 注意是小数才行，否则报错
		// new CustomNumberComparator(Bytes.toBytes(18),"int" ) // 支持性不好 
		// new CustomNumberComparator(Bytes.toBytes(18.0),"float" ) // 支持性不好 估计是因为版本兼容问题，用的protoc 2.5 32位编译出来的java文件
		); 
		filterList.addFilter(low_bound); 
		
		SingleColumnValueFilter up_bound = new SingleColumnValueFilter( 
		family, 
		col_age, 
		CompareFilter.CompareOp.LESS_OR_EQUAL, 
		new CustomNumberComparator(Bytes.toBytes(80.0),"double" ) 
		// new CustomNumberComparator(Bytes.toBytes(18),"int" ) // 支持性不好 
		// new CustomNumberComparator(Bytes.toBytes(18.0),"float" ) // 支持性不好 
		); 
		filterList.addFilter(up_bound); 
		
		s.setFilter(filterList);
		
		s.addColumn(family, col_name);
		s.addColumn(family, col_age);
		
		ResultScanner scanner = table.getScanner(s);
		Iterator<Result> iterator = scanner.iterator();
		
		// scanner
		try {
			while (iterator.hasNext()) {
				Result rs = iterator.next();
				User user = null;
				try {
					//user = UserParser.parser(rs,User.class);
					user = (User)ParserUtils.<User>parser2Bean(rs, User.class,"userId");
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println(user);
			}
			
		} finally {
			// 确保scanner关闭
			scanner.close();
		}

		if (table != null) {
			table.close();
		}
		
		// test end and delete table 
		// deleteTable(admin, userTable);
		conn.close();
	}

	private static void getTest(Table table) throws IOException {
		Get g = new Get(Bytes.toBytes("user0"));
		Result result = table.get(g);
		String value = Bytes.toString(result.getValue(family,
				col_name));
		System.out.println("GET user0 :" + value);
	}

	/**
	 * 测试删除数据
	 * @param table
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void deleteKey(Table table) throws IOException {
		// 删除某条数据,操作方式与 Put 类似
		Delete d = new Delete("id001".getBytes());
		d.addColumn(family, col_name);
		table.delete(d);
	}

	/**
	 * 测试完 之后删除表
	 * @param admin
	 * @param userTable
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void deleteTable(Admin admin, TableName userTable)
			throws IOException {
		admin.disableTable(userTable);
		 admin.deleteTable(userTable);
	}

	/**
	 * 如果表不存在将创建新表
	 * @param admin
	 * @param userTable
	 * @throws IOException
	 */
	private static void createTable(Admin admin, TableName userTable)
			throws IOException {
		// 创建 user 表
		 HTableDescriptor tableDescr = new HTableDescriptor(userTable);
		 tableDescr.addFamily(new HColumnDescriptor(family));
		 System.out.println("Creating table `user` ============ ");
		 if (!admin.tableExists(userTable)) {
			 admin.createTable(tableDescr);
		 }
		System.out.println("create table Done!");
	}

	/**
	 * 制造一些脏数据
	 * @param table
	 * @throws IOException
	 */
	private static void inserData(Table table) throws IOException {
	
		String[] values = new  String[] {"100","80","20","2","2.0","12.5",""," ","+",";","；"};
		for (int i = 0; i < values.length; i++) {
			// 准备插入几条 score不同的数据
			Put p = new Put(("user"+i).getBytes());
			// 为put操作指定 column 和 value （以前的 put.add 方法被弃用了）
			p.addColumn(family, col_name, ("name" + i).getBytes());
			p.addColumn(family, col_age, (values[i]).getBytes());
			// 提交
			table.put(p);	
		}
	}
}

//List<Cell> listCells = rs.listCells();
//for (Cell cell : listCells) {
//String key = Bytes.toString(CellUtil.cloneRow(cell));
//String cloneFamily = Bytes.toString(CellUtil.cloneFamily(cell));
//String cloneQualifier = Bytes.toString(CellUtil.cloneQualifier( cell));
//String cloneValue = Bytes.toString(CellUtil.cloneValue(cell));
////System.out.print("key:"+key + "==cloneFamily:" +cloneFamily +"==Qualifier:"+cloneQualifier+ "==value:"+cloneValue );
//}