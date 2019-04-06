package my.company;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.jayway.jsonpath.JsonPath;

public class HttpKeyWord {
	private boolean useCookie = false;
	public CookieStore cookies = new BasicCookieStore();
	private Map<String, String> header = new HashMap<String, String>();
	private boolean addHeaderFlag = false;
	private CloseableHttpClient client;
	public HttpClientContext context = HttpClientContext.create();
	private static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	// 用于转换Unicode编码为中文
	private String DeCode(String u) {
		try {
			Matcher m = reUnicode.matcher(u);
			StringBuffer sb = new StringBuffer(u.length());
			while (m.find()) {
				m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
			}
			m.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return u;
		}
	}

	// 用于绕过ssl验证，使发包的方法能够对https的接口进行请求
	public static SSLContext creatIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	// GET请求封装
	public String doGet(String url, String param) throws Exception {
		String body = "";
		SSLContext sslc = creatIgnoreVerifySSL();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslc)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// 设置context内容对象的cookiestore
		context.setCookieStore(cookies);
		// 以下代码为需要使用代理抓包时使用，并根据是否使用cookies来选择不同的方式构建httpclient
//	    HttpHost proxy = new HttpHost("localhost",8888,"http");
//	    if(useCookie) {
//	    	client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
//	    }else {
//	    	client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//	    }

		// 不需要使用代理抓包时使用以下代码，并根据是否使用cookies来选择不同的方式构建httpclient
		if (useCookie) {
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		param=URLEncoder.encode(param, "utf-8");
		try {
			// 拼接url地址和参数列表
			String urlAndParam = "";
			if (param.length() > 0) {
				urlAndParam = url + "?" + param;
			} else {
				urlAndParam = url;
			}

			// 创建get方法请求对象
			HttpGet get = new HttpGet(urlAndParam);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(1000).build();
			get.setConfig(config);

			// 指定报文头Content-type、User_Agent
			get.setHeader("accept", "*/*");
			get.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");

			// 通过是否添加头域的标志服判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = header.keySet();
				for (String key : headerKeys) {
					get.setHeader(key, header.get(key));
				}
			}

			// 执行请求操作
			CloseableHttpResponse response = client.execute(get);

			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println("Cookie:" + c);
			}

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				body = EntityUtils.toString(entity, "UTF-8");
			}

			// 关闭流实体
			EntityUtils.consume(entity);

			// 释放链接
			response.close();
			String result = DeCode(body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.printStackTrace();
		} finally {
			// TODO Auto-generated catch block
			client.close();
		}

		return null;
	}

	// POST请求封装
	public String doPost(String url, String param) throws Exception {
		String body = "";
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = creatIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");
//		// 基于是否需要使用cookie，用不同方式创建httpclient实例，决定是否添加cookiestore设置
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager)
//					.setDefaultCookieStore(cookies).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}

		// 不使用代理抓包时，用该段代码。
		// 基于是否需要使用cookie，用不同方式创建httpclient实例，决定是否添加cookiestore设置
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		// 拼接接口地址和参数
		try {
			String urlWithParam = "";
			if (param.length() > 0) {
				urlWithParam = url + "?" + param;
			} else {
				urlWithParam = url;
			}

			// 创建post方式请求对象
			HttpPost httpPost = new HttpPost(urlWithParam);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			httpPost.setConfig(config);
			// 指定报文头Content-type、User-Agent
			httpPost.setHeader("accept", "*/*");
			httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = header.keySet();
				for (String key : headerKeys) {
					httpPost.setHeader(key, header.get(key));
				}
			}

			// 执行请求操作，并拿到结果
			CloseableHttpResponse response = client.execute(httpPost);

			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println("Cookie:" + c);
			}

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换结果实体为String类型
				body = EntityUtils.toString(entity, "UTF-8");
			}

			EntityUtils.consume(entity);
			// 释放链接
			response.close();
			String result = DeCode(body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	// 使用json传参的POST请求
	public String doPostJson(String url, String jsonparam) throws Exception {
		String body = "";
		SSLContext sslcontext = creatIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		HttpClients.custom().setConnectionManager(connManager);

		// 当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//						HttpHost proxy = new HttpHost("localhost", 8888, "http");  
//						//基于是否需要使用cookie，用不同方式创建httpclient实例，决定是否添加cookiestore设置
//						if (useCookie) {
//							client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
//						} else {
//							client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//						}

		// 不使用代理抓包时，用该段代码。
		// 基于是否需要使用cookie，用不同方式创建httpclient实例，决定是否添加cookiestore设置
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		try {
			// 创建post请求对象
			HttpPost httpPost = new HttpPost(url);
			// 指定报文头域信息
			httpPost.setHeader("accept", "*/*");
			httpPost.setHeader("Content-type", "application/json;charset=UTF-8");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 传递json格式参数
			StringEntity jsonReq = new StringEntity(jsonparam);
			jsonReq.setContentEncoding("UTF-8");
			jsonReq.setContentType("application/json");
			httpPost.setEntity(jsonReq);

			// 执行请求并获取结果
			CloseableHttpResponse response = client.execute(httpPost);

			// 获取响应实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				body = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			// 释放链接
			response.close();
			String result = DeCode(body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	// 设置需要使用cookie
	public void saveCookie() {
		useCookie = true;
	}

	// 设置不使用cookie，且清除cookie
	public void clearCookie() {
		useCookie = false;
		cookies = new BasicCookieStore();
	}

	// 设置使用头域，并传递头域信息
	public void addHeader(Map<String, String> headerMap) {
		header = headerMap;
		addHeaderFlag = true;
	}

	// 设置不使用头域，且清空头域信息
	public void clearHeader() {
		addHeaderFlag = false;
		header = new HashMap<String, String>();
	}

	// json断言相等
	public void assertEqual(String jsonStr, String jsonPath, String asserStr) {
		String re = JsonPath.read(jsonStr, jsonPath).toString();
		if (re.equals(asserStr)) {
			System.out.println("Success");
		} else {
			System.out.println("Fail");
		}
	}

	// json断言包含
	public void assertContains(String jsonStr, String jsonPath, String asserStr) {
		String re = JsonPath.read(jsonStr, jsonPath).toString();
		if (re.contains(asserStr)) {
			System.out.println("Success");
		} else {
			System.out.println("Fail");
		}
	}
}
