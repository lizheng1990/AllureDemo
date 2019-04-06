package my.company;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.sun.corba.se.impl.util.SUNVMCID;
import com.testing.common.AutoLogger;
import com.testing.common.ExcelWriter;

public class KeyWord {

	//用于应用之前封装的关键字类
	public HttpKeyWord client;
	//用于存储请求中的相应信息
	public Map<String,String> paramMap;
	//用于存储响应信息
	public String temResponse;
	//新建一个操作的excelwriter
	public ExcelWriter ew;
	//成员变量，用于在用例中执行时保持执行行和写入一致
	public int line = 0;
	
	//新建一个构造方法来创建client和paramMap
	public KeyWord() {
		client = new HttpKeyWord();
		paramMap = new HashMap<String,String>();
	}
	
	//新建一个构造方法来创建client和paramMap,外部创建好excelwriter对象之后，直接传递给构造方法使用
	public KeyWord(ExcelWriter excel) {
		client = new HttpKeyWord();
		paramMap = new HashMap<String,String>();
		ew = excel;
	}
	
	//新建一个构造方法来创建client和paramMap,传递用例文件和结果文件路径，在构造方法中实例化excelwriter对象
	public KeyWord(String casePath,String resultPath) {
		client = new HttpKeyWord();
		paramMap = new HashMap<String,String>();
		ew = new ExcelWriter(casePath, resultPath);
	}
	
	//重新封装GET请求
	public String GetRequest(String url,String paramStr) {
		String param = toParam(paramStr);
		try {
			temResponse = client.doGet(url, param);
			AutoLogger.log.info("发送GEt请求：" + url + "成功");
			ew.writeCell(line, 10, "PASS");
			return temResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("发送GEt请求：" + url + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return temResponse;
		}
	}
	
	//重新封装POST请求
	public String PostRequest(String url,String paramStr) {
		String param = toParam(paramStr);
		try {
			temResponse = client.doPost(url, param);
			AutoLogger.log.info("发送POST请求：" + url + "成功");
			ew.writeCell(line, 10, "PASS");
			return temResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("发送POST请求：" + url + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return temResponse;
		}
	}
	
	//重新封装以JSON格式发送POST的请求
	public String PostJsonRequest(String url,String paramStr) {
		String param = toParam(paramStr);
		try {
			param = URLEncoder.encode(param, "UTF-8");
			temResponse = client.doPostJson(url, param);
			AutoLogger.log.info("发送JSON格式的POST请求：" + url + "成功");
			ew.writeCell(line, 10, "PASS");
			return temResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("发送JSON格式的POST请求：" + url + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return temResponse;
		}
	}
	
	//封装Rest接口的get请求
	public String RestGet(String url) {
		String acturl = toParam(url);
		try {
			temResponse = client.doGet(acturl, "");
			AutoLogger.log.info("发送Rest的Get请求：" + url + "成功");
			ew.writeCell(line, 10, "PASS");
			return temResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("发送Rest的Get请求：" + url + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return temResponse;
		}
	}
	
	//封装Rest接口的post请求
	public String RestPost(String url) {
		String acturl = toParam(url);
		try {
			temResponse = client.doPost(acturl, "");
			AutoLogger.log.info("发送Rest的Post请求：" + url + "成功");
			ew.writeCell(line, 10, "PASS");
			return temResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("发送Rest的Post请求：" + url + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return temResponse;
		}
	}
	
	//封装保存cookie方法
	public void savaCookie() {
		try {
			client.saveCookie();
			ew.writeCell(line, 10, "PASS");
			AutoLogger.log.info("保存cookie信息成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("保存cookie信息失败");
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
		}
	}
	
	//封装清除cookie方法
	public void clearCookie() {
		try {
			client.clearCookie();
			ew.writeCell(line, 10, "PASS");
			AutoLogger.log.info("清除cookie信息成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("清除cookie信息失败");
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
		}
	}
	
	//重新封装使用头域信息,且接受格式为JSON格式
	public void addHeader(String paramJson) {
		Map<String,String> jsonMap = new HashMap<String,String>();
		String headerJson = toParam(paramJson);
		try {
			JSONObject json = new JSONObject(headerJson);
			Iterator<String> it = json.keys();
			while(it.hasNext()) {
				String key = it.next();
				jsonMap.put(key, json.getString(key).toString());
				client.addHeader(jsonMap);
				AutoLogger.log.info("增加头域信息：" + jsonMap + "成功");
				ew.writeCell(line, 10, "PASS");
			}
		} catch (JSONException e) {
			AutoLogger.log.info("增加头域信息：" + headerJson + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			client.addHeader(jsonMap);
		}
	}
	
	//封装清除头域信息
	public void clearHeader() {
		try {
			client.clearHeader();
			ew.writeCell(line, 10, "PASS");
			AutoLogger.log.info("清除头域信息成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("清除头域信息失败");
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
		}
	}
	
	//构建一个替换字符串中满足指定格式的值的方法
	public String toParam(String origin) {
		String param = origin;
		try {
			for(String key:paramMap.keySet()) {
				param = param.replaceAll("\\{" + key + "\\}", paramMap.get(key));
			}
			AutoLogger.log.info("替换" + origin + "为" + param + "成功");
			return param;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("替换" + origin + "为" + param + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			return param;
		}
	}
	
	//构建一个保存指定json元素值到字典paramMap的方法
	public void savaParam(String key,String jsonPath) {
		String value;
		try {
			value = JsonPath.read(temResponse, jsonPath).toString();
			paramMap.put(key, value);
			ew.writeCell(line, 10, "PASS");
			AutoLogger.log.info("保存" + jsonPath + "的值成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("保存" + jsonPath + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
		}
	}
	
	//封装获取响应数据中的值
	public String returnValue(String response,String jsonPath) {
		String value = null;
		String re;
		try {
			re = JsonPath.read(response,jsonPath).toString();
			value = re;
			AutoLogger.log.info("获取响应中的值" + jsonPath +"成功");
			return value;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("获取响应中的值" + jsonPath + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			return value;
		}
	}
	
	//封装equal断言
	public boolean assertEqual(String jsonPath,String assertStr) {
		boolean SUCCESS= false;
		try {
			String actual = JsonPath.read(temResponse, jsonPath).toString();
			if (actual != null && actual.equals(assertStr)) {
				AutoLogger.log.info("测试通过!");
				ew.writeCell(line, 10, "PASS");
				SUCCESS = true;
				return SUCCESS;
			}else {
				AutoLogger.log.error("测试失败");
				ew.writeFailCell(line, 10, "FAIL");
				ew.writeFailCell(line, 11, "结果：" + actual + "不等于预期值：" + assertStr);
				SUCCESS = false;
				return SUCCESS;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("解析" + jsonPath + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return SUCCESS;
		}
	}
	
	//封装equal断言
	public boolean assertContains(String jsonPath,String assertStr) {
		boolean SUCCESS= false;
		try {
			String actual = JsonPath.read(temResponse, jsonPath).toString();
			if (actual != null && actual.contains(assertStr)) {
				AutoLogger.log.info("测试通过!");
				ew.writeCell(line, 10, "PASS");
				SUCCESS = true;
				return SUCCESS;
			}else {
				AutoLogger.log.error("测试失败");
				ew.writeFailCell(line, 10, "FAIL");
				ew.writeFailCell(line, 11, "结果：" + actual + "不包含预期值：" + assertStr);
				SUCCESS = false;
				return SUCCESS;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AutoLogger.log.error("解析" + jsonPath + "失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			ew.writeFailCell(line, 10, "FAIL");
			ew.writeFailCell(line, 11, e.fillInStackTrace().toString());
			return SUCCESS;
		}
	}
}
