package test;

import java.lang.reflect.Constructor;  
import java.lang.reflect.Method;  

import org.junit.Test;   

import com.pateo.bean.User;
public class RefTest {  
    @Test  
    public void testRef() throws Exception {  
        Object obj = getBean("com.pateo.bean.User");  
        this.setProperty(obj, "name", "admin");  
        this.setProperty(obj, "age", 12);  
        User user = (User) obj; //对应ClassPathXmlApplicationContext.getBean(id)  
        System.out.println(user.getName());  
        System.out.println(user.getAge());         
    }  
    public static void main(String[] args) throws Exception {
		
    	Object obj = getBean("com.pateo.bean.User");  
        setProperty(obj, "name", "admin");  
        setProperty(obj, "age", 12);  
        User user = (User) obj; //对应ClassPathXmlApplicationContext.getBean(id)  
        System.out.println(user.getName());  
        System.out.println(user.getAge());
	}
    public static Object getBean(String className) throws Exception {  
        Class cls = null;  
        try {  
            cls = Class.forName(className);//对应Spring ->bean -->class  
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
            throw new Exception("类错误！");  
        }  
        Constructor[] cons = null;//得到所有构造器  
        try {  
            cons = cls.getConstructors();  
        } catch (Exception e) {  
            e.printStackTrace();  
            throw new Exception("构造器错误！");  
        }  
        if (cons == null || cons.length < 1) {  
            throw new Exception("没有默认构造方法！");  
        }  
        //如果上面没错，就有构造方法  
        Constructor defCon = cons[0];//得到默认构造器,第0个是默认构造器，无参构造方法  
        Object obj = defCon.newInstance();//实例化，得到一个对象 //Spring - bean -id  
        return obj;  
    }  
    public static void setProperty(Object bean, String propertyName, Object propertyValue) throws Exception {  
        Class cls = bean.getClass();  
        Method[] methods = cls.getMethods();//得到所有方法  
        for (Method m : methods) {  
            if (m.getName().equalsIgnoreCase("set" + propertyName)) {  
                //找到方法就注入  
                m.invoke(bean, propertyValue);  
                break;  
            }  
        }  
    }  
}