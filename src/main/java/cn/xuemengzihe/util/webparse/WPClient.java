package cn.xuemengzihe.util.webparse;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xuemengzihe.util.webparse.conf.ConfigName;
import cn.xuemengzihe.util.webparse.conf.WebParseConfig;
import cn.xuemengzihe.util.webparse.exception.ConnectException;
import cn.xuemengzihe.util.webparse.exception.ErrorParseException;
import cn.xuemengzihe.util.webparse.exception.URLException;
import cn.xuemengzihe.util.webparse.parse.PageParseUtil;

/**
 * <h1>WebParse 客户端</h1>
 * <p>
 * 通过该类可以实现对教学网的访问<br/>
 * </p>
 * 
 * @author 李春
 * @time 2017年5月1日 下午8:50:14
 */
public class WPClient implements Closeable {

	private Logger logger = LoggerFactory.getLogger(WPClient.class);

	/**
	 * 用户角色：部门
	 */
	public static final String USER_ROLE_DEPARTMENT = "部门";
	/**
	 * 用户角色：教师
	 */
	public static final String USER_ROLE_TEACHER = "教师";
	/**
	 * 用户角色：学生
	 */
	public static final String USER_ROLE_STUDENT = "学生";
	private String userSNO; // 学号（登录账号）
	private String userName; // 用户名称
	private String role; // 用户类型（部门，老师，学生）
	private String password; // 用户密码
	private String referer; // referer头
	private String subUrl; // 子url
	private CloseableHttpClient httpClient; // HttpClient

	private void login() {
		String url = WebParseConfig.getConfig(ConfigName.WEB_URL);
		String tempSubUrl = null;
		HttpPost postReq = null;
		HttpResponse response = null;
		try {
			// 第一次访问沈阳理工大学教学网的教学网时，第一次接收到的是重定向（状态码为302）
			response = this.httpClient.execute(new HttpPost(url)); // 访问教学网首页
			// 检验第一次访问是否得到302的状态码
			if (response.getStatusLine().getStatusCode() != 302) {
				logger.error("PageOperateUtil: the first request return a unexpected statusCode "
						+ response.getStatusLine().getStatusCode()
						+ ", not 302!");
				throw new RuntimeException("登录失败！");
			}
			// 获得预期状态码，继续登录
			// 1. 获取登录界面的URL
			tempSubUrl = response.getFirstHeader("Location").getValue();
			logger.debug("First Request:the return location is " + tempSubUrl);
			// 2. 获取登录界面
			String loginPageContent = null;
			postReq = new HttpPost(url + tempSubUrl);
			response = this.httpClient.execute(postReq);
			// this.cookie = response.getFirstHeader("Set-Cookie").getValue();
			// 设置Cookie
			loginPageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Login Page Content:");
			logger.debug(loginPageContent);
			// 3. 从登录界面获取相应的参数 并为登录请求准备参数
			String viewState = PageParseUtil
					.getParam__VIEWSTATE(loginPageContent); // 从页面中获取__VIEWSTATE
			// 4. 封装登录请求参数
			List<NameValuePair> nameValuePairLogin = packageLoginParams(viewState);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
					nameValuePairLogin,
					WebParseConfig.getConfig(ConfigName.PAGE_ENCODING));// 设置参数到请求中
			// 5. 发送登录请求
			postReq = new HttpPost(url + tempSubUrl);
			postReq.setEntity(entity);
			response = this.httpClient.execute(postReq);
			// 第三步:判断登录请求是否成功（成功返回302）
			if (response.getStatusLine().getStatusCode() != 302) {
				logger.error("Login failed because of connect errer");
				throw new RuntimeException("登录失败！");
			}
			tempSubUrl = response.getFirstHeader("Location").getValue();
			this.referer = WebParseConfig.getConfig(ConfigName.WEB_URL)
					+ tempSubUrl; // 设置Referrer
			logger.debug("PageOperateUtil: redirect url when sended login request:"
					+ tempSubUrl);
			logger.debug("Page Content:");
			logger.debug(PageParseUtil.getHTMLContent(response));
			postReq = new HttpPost(url + tempSubUrl);
			response = this.httpClient.execute(postReq);
			this.subUrl = tempSubUrl.substring(0, tempSubUrl.lastIndexOf("/")); // 设置subUrl
			String content = PageParseUtil.getHTMLContent(response);
			this.userName = PageParseUtil.getUserName(content); // 获取用户名
			logger.debug("Page Content:");
			logger.debug(content);
			logger.debug("PageOperateUtil: login success!");
		} catch (IOException e) {
			logger.error("PageOperateUtil: there is a exeption happened, login abort!");
			e.printStackTrace();
			throw new RuntimeException("登录失败！");
		} catch (ErrorParseException e) {
			logger.error("PageOperateUtil: a error cathed when parse the web content!");
			e.printStackTrace();
			throw new RuntimeException("登录失败！");
		}
	}

	/**
	 * 创建学生客户端
	 * 
	 * @param userSNO
	 *            学号
	 * @param password
	 *            密码
	 */
	public WPClient(String userSNO, String password) {
		this.role = USER_ROLE_STUDENT;
		this.userSNO = userSNO;
		this.password = password;
		httpClient = HttpClients.createDefault(); // 创建客户端
		this.login();
	}

	/**
	 * 创建客户端
	 * 
	 * @param userSNO
	 *            用户名
	 * @param password
	 *            密码
	 * @param role
	 *            用户角色（取值：{@link #USER_ROLE_DEPARTMENT} 、
	 *            {@link #USER_ROLE_TEACHER}、{@link #USER_ROLE_STUDENT}）
	 */
	public WPClient(String userSNO, String password, String role) {
		this.role = role;
		this.userSNO = userSNO;
		this.password = password;
		httpClient = HttpClients.createDefault(); // 创建客户端
		this.login();
	}

	/**
	 * 查询成绩
	 * 
	 * @param wpClient
	 *            WPClient
	 * @param grade
	 *            学年
	 * @param term
	 *            学期
	 * @throws ErrorParseException
	 *             页面解析失败异常
	 * @return 成绩页面的HTML文本
	 */
	public List<Map<String, String>> queryScore(String grade, String term,
			String btnName) {
		HttpResponse response = null;
		HttpPost postReq = null;
		String pageContent = null;
		String url = null;
		try {
			url = getUserURL(ConfigName.PAGE_XSCJCX); // 获取URL
			postReq = new HttpPost(url);
			postReq.setHeader("Referer", this.referer);
			response = this.httpClient.execute(postReq);
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Score Query Page Content:");
			logger.debug(pageContent);
			// 封装请求参数
			String viewState = PageParseUtil.getParam__VIEWSTATE(pageContent);
			UrlEncodedFormEntity entity = packageScoreParams(grade, term,
					btnName, viewState);
			postReq.setEntity(entity);
			postReq.setHeader("Referer", this.referer);
			response = this.httpClient.execute(postReq);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("connect failed because unexpected status code!");
				throw new ConnectException();
			}
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Score Page:");
			logger.debug(pageContent);
			return PageParseUtil.getScoreResult(pageContent);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("成绩解析失败");
		}
	}

	/**
	 * 查询学生课表
	 * 
	 * @param grade
	 *            年度（例如2013-2014）
	 * @param term
	 *            学期（1,2,3）
	 * @return 课表查询页的内容（HTML文本）
	 * @throws ConnectException
	 */
	public String queryClassSheet(String grade, String term) {
		HttpResponse response = null;
		HttpPost postReq = null;
		String pageContent = null;
		String url = null;
		try {
			url = getUserURL(ConfigName.PAGE_XSKBCX); // 获取URL
			postReq = new HttpPost(url);
			postReq.setHeader("Referer", this.referer);
			response = httpClient.execute(postReq);
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Class Sheet Query Page Content:");
			logger.debug(pageContent);
			// 封装请求参数
			String viewState = PageParseUtil.getParam__VIEWSTATE(pageContent);
			UrlEncodedFormEntity entity = packageKBCXParams(grade, term,
					viewState);
			postReq.setEntity(entity); // 设置查询参数
			postReq.setHeader("Referer", this.referer);
			response = httpClient.execute(postReq);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("connect failed because unexpected status code!");
				throw new ConnectException();
			}
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Class Sheet Query Result Page:");
			logger.debug(pageContent);
			return pageContent;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("课表查询失败！");
		}
	}

	/**
	 * 封装成绩查询参数
	 * 
	 * @param grade
	 *            年度（例如2013-2014）
	 * @param term
	 *            学期（1,2,3）
	 * @param btnName
	 *            按钮名称
	 * @param viewState
	 *            viewState
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private UrlEncodedFormEntity packageScoreParams(String grade, String term,
			String btnName, String viewState)
			throws UnsupportedEncodingException {
		List<NameValuePair> queryGradePair = new ArrayList<NameValuePair>();
		queryGradePair.add(new BasicNameValuePair("__EVENTTARGET", ""));
		queryGradePair.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		queryGradePair.add(new BasicNameValuePair("__VIEWSTATE", viewState));
		queryGradePair.add(new BasicNameValuePair("hidLanguage", ""));
		queryGradePair.add(new BasicNameValuePair("ddlXN", grade));// 学年
		queryGradePair.add(new BasicNameValuePair("ddlXQ", term));// 学期
		queryGradePair.add(new BasicNameValuePair("ddl_kcxz", ""));
		switch (btnName) {
		case ConfigName.BTN_学期成绩:
			queryGradePair.add(new BasicNameValuePair(btnName, "学期成绩"));
			break;
		case ConfigName.BTN_学年成绩:
			queryGradePair.add(new BasicNameValuePair(btnName, "学年成绩"));
			break;
		case ConfigName.BTN_历年成绩:
			queryGradePair.add(new BasicNameValuePair(btnName, "历年成绩"));
			break;
		case ConfigName.BTN_课程最高成绩:
			queryGradePair.add(new BasicNameValuePair(btnName, "课程最高成绩"));
			break;
		case ConfigName.BTN_未通过成绩:
			queryGradePair.add(new BasicNameValuePair(btnName, "未通过成绩"));
			break;
		case ConfigName.BTN_成绩统计:
			queryGradePair.add(new BasicNameValuePair(btnName, "成绩统计"));
			break;
		default:
			queryGradePair.add(new BasicNameValuePair(btnName, "历年成绩"));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(queryGradePair);
		return entity;
	}

	/**
	 * 封装课表查询的参数
	 * 
	 * @param grade
	 *            年度（例如2013-2014）
	 * @param term
	 *            学期（1,2,3）
	 * @param viewState
	 *            viewState
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private UrlEncodedFormEntity packageKBCXParams(String grade, String term,
			String viewState) throws UnsupportedEncodingException {
		List<NameValuePair> queryClassSheetPair = new ArrayList<NameValuePair>();
		queryClassSheetPair.add(new BasicNameValuePair("__EVENTTARGET", "xnd"));
		queryClassSheetPair.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		queryClassSheetPair
				.add(new BasicNameValuePair("__VIEWSTATE", viewState));
		queryClassSheetPair.add(new BasicNameValuePair("xnd", grade));// 学年
		queryClassSheetPair.add(new BasicNameValuePair("xqd", term));// 学期
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
				queryClassSheetPair);
		return entity;
	}

	/**
	 * 生成页面的URL
	 * 
	 * @param configName
	 *            页面名称(取值:{@link ConfigName#PAGE_XSCJCX}......)
	 * @return 页面的URL
	 * @throws URLException
	 * @throws UnsupportedEncodingException
	 */
	private String getUserURL(String configName) throws URLException,
			UnsupportedEncodingException {
		String url = WebParseConfig.getConfig(ConfigName.WEB_URL) + this.subUrl
				+ WebParseConfig.getConfig(configName);
		String[] temp = url.split(",");
		url = "";
		if (temp.length != 3)
			throw new URLException();
		for (int i = 0; i < temp.length; i++) {
			switch (i) {
			case 1:
				url += this.userSNO;
				break;
			case 2:
				url += URLEncoder.encode(this.userName,
						WebParseConfig.getConfig(ConfigName.PAGE_ENCODING)); // 对姓名进行URL编码
				break;
			}
			url += temp[i];
		}
		logger.debug("Query URL:" + url);
		return url;
	}

	/**
	 * 封装登录请求参数
	 * 
	 * @param viewState
	 *            viewState
	 * @return
	 */
	private List<NameValuePair> packageLoginParams(String viewState) {
		List<NameValuePair> nameValuePairLogin = new ArrayList<NameValuePair>();
		nameValuePairLogin
				.add(new BasicNameValuePair("__VIEWSTATE", viewState));// 隐藏表单值
		nameValuePairLogin
				.add(new BasicNameValuePair("TextBox1", this.userSNO));// 学号
		nameValuePairLogin
				.add(new BasicNameValuePair("TextBox2", this.password));// 密码
		nameValuePairLogin.add(new BasicNameValuePair("RadioButtonList1",
				this.role));// 用户角色
		nameValuePairLogin.add(new BasicNameValuePair("Button1", ""));
		nameValuePairLogin.add(new BasicNameValuePair("lbLanguage", ""));
		return nameValuePairLogin;
	}

	@Override
	public void close() throws IOException {
		try {
			this.httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("WPClient: Logout!");
	}

}
