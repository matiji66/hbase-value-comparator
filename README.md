这个自定义的Comparator也是看了好多资料.
此处是基于cdh-hbase1.2.0 分享自定义hbase filter全过程(此处的Comparator是针对SingleColumnFilter进行测试的,其他的过滤器并没具体测试)。

1.制作 CustomComparatorProtos.proto文件

先编辑一个文件命名为CustomComparatorProtos.proto 
具体内容如下： 
/** 
* Licensed to the Apache Software Foundation (ASF) under one 
* or more contributor license agreements. See the NOTICE file 
* distributed with this work for additional information 
* regarding copyright ownership. The ASF licenses this file 
* to you under the Apache License, Version 2.0 (the 
* "License"); you may not use this file except in compliance 
* with the License. You may obtain a copy of the License at 
* 
* http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License. 
*/

// This file contains protocol buffers that are used for filters

option java_package = "com.pateo.hbase.defined.comparator"; 
option java_outer_classname = "MyComparatorProtos"; 
option java_generic_services = true; 
option java_generate_equals_and_hash = true; 
option optimize_for = SPEED;

// This file contains protocol buffers that are used for comparators (e.g. in filters)

message CustomNumberComparator { 
required bytes value = 1; 
required string fieldType = 2; 
}


2.
使用protoc编辑器进行编译 
在protoc 当前的目录 打开终端进入当前目录执行 
CMD 执行如下命令 ：
protoc –java_out=D:\proto CustomNumberComparator.proto 
然后得到MyComparatorProtos.java

3.
创建package和上面的填写额一致（com.pateo.hbase.defined.comparator），将MyComparatorProtos.java导入自己定义的package下面，
并且在该package下面创建CustomNumberComparator.java继承ByteArrayComparable 。
 
//具体代码 参考该package下的com.pateo.hbase.defined.comparator.CustomNumberComparator源文件，此处省略。


4.选中这两个java文件右击export 输出为jar文件 
上传到hbase的lib目录下面，重启hbase，在hbase shell中引入以下 java包

import org.apache.hadoop.hbase.filter.CompareFilter 
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter 
import com.pateo.hbase.defined.comparator.CustomNumberComparator
import com.pateo.hbase.defined.comparator.MyComparatorProtos


scan 'student',{COLUMNS =>'score:chinese',FILTER=>SingleColumnValueFilter.new(Bytes.toBytes('score'),Bytes.toBytes('chinese'),CompareFilter::CompareOp.valueOf('GREATER'),CustomNumberComparator.new(Bytes.toBytes(2.0),"double"))}


5. java 代码测试参考 test.TestHbaseJava


注意事项：列值为'', ' ' '+' 等会导致filter失败，应该是数值转换失败导致的 