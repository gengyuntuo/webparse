package cn.xuemengzihe.util.webparse.parse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xuemengzihe.util.webparse.WPClient;
import cn.xuemengzihe.util.webparse.conf.ConfigName;
import cn.xuemengzihe.util.webparse.conf.WebParseConfig;
import cn.xuemengzihe.util.webparse.exception.ConnectException;
import cn.xuemengzihe.util.webparse.exception.ErrorParseException;
import cn.xuemengzihe.util.webparse.exception.URLException;

/**
 * <h1>教学网的操作</h1>
 * <p>
 * 该类封装了对教学网的一些操作（登录、页面请求、注销）
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 上午11:49:06
 */
public class PageOperateUtil {
	private static Logger logger = LoggerFactory
			.getLogger(PageOperateUtil.class);

	/**
	 * 根据客户端生成页面的URL
	 * 
	 * @param wpClient
	 *            客户端
	 * @param configName
	 *            页面名称
	 * @return 页面的URL
	 * @throws URLException
	 * @throws UnsupportedEncodingException
	 */
	private static String getUserURL(WPClient wpClient, String configName)
			throws URLException, UnsupportedEncodingException {
		String url;
		url = WebParseConfig.getConfig(ConfigName.WEB_URL)
				+ wpClient.getSubUrl() + WebParseConfig.getConfig(configName);
		String[] temp = url.split(",");
		url = "";
		if (temp.length != 3)
			throw new URLException();
		for (int i = 0; i < temp.length; i++) {
			switch (i) {
			case 1:
				url += wpClient.getUserSNO();
				break;
			case 2:
				url += URLEncoder.encode(wpClient.getUserName(),
						WebParseConfig.getConfig(ConfigName.PAGE_ENCODING)); // 对姓名进行URL编码
				break;
			}
			url += temp[i];
		}
		logger.debug("Query URL:" + url);
		return url;
	}

	/**
	 * 登录
	 * 
	 * @param wpClient
	 *            WPClient
	 * @param userSNO
	 *            用户学号
	 * @param password
	 *            密码
	 * @throws ConnectException
	 *             连接失败，抛出连接失败异常
	 */
	public static void login(WPClient wpClient, String userSNO, String password)
			throws ConnectException {
		HttpClient client = wpClient.getHttpClient();
		String url = WebParseConfig.getConfig(ConfigName.WEB_URL);
		String subUrl = null;
		HttpPost postReq = null;
		HttpResponse response = null;
		try {
			// 第一次访问沈阳理工大学教学网的教学网时，第一次接收到的是重定向（状态码为302）
			response = client.execute(new HttpPost(url)); // 访问教学网首页
			// 检验第一次访问是否得到302的状态码
			if (response.getStatusLine().getStatusCode() != 302) {
				logger.error("PageOperateUtil: the first request return a unexpected statusCode "
						+ response.getStatusLine().getStatusCode()
						+ ", not 302!");
				throw new ConnectException();
			}
			// 获得预期状态码，继续登录
			// 1. 获取登录界面的URL
			subUrl = response.getFirstHeader("Location").getValue();
			logger.debug("First Request:the return location is " + subUrl);
			// 2. 获取登录界面
			String loginPageContent = null;
			postReq = new HttpPost(url + subUrl);
			postReq.setHeader("Cookie", wpClient.getCookie());
			response = client.execute(postReq);
			wpClient.setCookie(response.getFirstHeader("Set-Cookie").getValue()); // 设置Cookie
			loginPageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Login Page Content:");
			logger.debug(loginPageContent);
			// 3. 从登录界面获取相应的参数 并为登录请求准备参数
			String viewState = PageParseUtil
					.getParam__VIEWSTATE(loginPageContent); // 从页面中获取__VIEWSTATE
			// 4. 封装登录请求参数
			List<NameValuePair> nameValuePairLogin = new ArrayList<NameValuePair>();
			nameValuePairLogin.add(new BasicNameValuePair("__VIEWSTATE",
					viewState));// 隐藏表单值
			nameValuePairLogin.add(new BasicNameValuePair("TextBox1", userSNO));// 学号
			nameValuePairLogin
					.add(new BasicNameValuePair("TextBox2", password));// 密码
			nameValuePairLogin.add(new BasicNameValuePair("RadioButtonList1",
					"学生"));// 身份,默认学生
			nameValuePairLogin.add(new BasicNameValuePair("Button1", ""));
			nameValuePairLogin.add(new BasicNameValuePair("lbLanguage", ""));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
					nameValuePairLogin,
					WebParseConfig.getConfig(ConfigName.PAGE_ENCODING));// 设置参数到请求中
			// 4. 发送登录请求
			postReq = new HttpPost(url + subUrl);
			postReq.setEntity(entity);
			response = client.execute(postReq);
			// 第三步:判断登录请求是否成功（成功返回302）
			if (response.getStatusLine().getStatusCode() != 302) {
				logger.error("Login failed because of connect errer");
				throw new ConnectException();
			}
			subUrl = response.getFirstHeader("Location").getValue();
			wpClient.setReferer(WebParseConfig.getConfig(ConfigName.WEB_URL)
					+ subUrl); // 获取Referrer
			logger.debug("PageOperateUtil: redirect url when sended login request:"
					+ subUrl);
			logger.debug("Page Content:");
			logger.debug(PageParseUtil.getHTMLContent(response));
			postReq = new HttpPost(url + subUrl);
			postReq.setHeader("Cookie", wpClient.getCookie());
			response = client.execute(postReq);
			wpClient.setSubUrl(subUrl.substring(0, subUrl.lastIndexOf("/"))); // 设置subUrl
			logger.debug("Page Content:");
			logger.debug(PageParseUtil.getHTMLContent(response));
			logger.debug("PageOperateUtil: login success!");
		} catch (IOException e) {
			logger.error("PageOperateUtil: there is a exeption happened, login abort!");
			e.printStackTrace();
			throw new ConnectException();
		} catch (ErrorParseException e) {
			logger.error("PageOperateUtil: a error cathed when parse the web content!");
			e.printStackTrace();
			throw new ConnectException();
		}
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
	public static String queryScore(WPClient wpClient, String grade, String term)
			throws ErrorParseException {
		HttpClient client = wpClient.getHttpClient();
		HttpResponse response = null;
		HttpPost postReq = null;
		String pageContent = null;
		String url = null;
		try {
			url = getUserURL(wpClient, ConfigName.PAGE_XSCJCX); // 获取URL
			postReq = new HttpPost(url);
			// postReq.setHeader("Cookie", wpClient.getCookie());
			postReq.setHeader("Referer", wpClient.getReferer());
			response = client.execute(postReq);
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Score Query Page Content:");
			logger.debug(pageContent);
			// 封装请求参数
			String viewState = PageParseUtil.getParam__VIEWSTATE(pageContent);
			List<NameValuePair> queryGradePair = new ArrayList<NameValuePair>();
			queryGradePair.add(new BasicNameValuePair("__EVENTTARGET", ""));
			queryGradePair.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			queryGradePair
					.add(new BasicNameValuePair("__VIEWSTATE", viewState));
			queryGradePair.add(new BasicNameValuePair("hidLanguage", ""));
			queryGradePair.add(new BasicNameValuePair("ddlXN", grade));// 学年
			queryGradePair.add(new BasicNameValuePair("ddlXQ", term));// 学期
			queryGradePair.add(new BasicNameValuePair("ddl_kcxz", ""));
			queryGradePair.add(new BasicNameValuePair("btn_zcj", "历年成绩"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
					queryGradePair);
			postReq.setEntity(entity);
			// postReq.setHeader("Cookie", wpClient.getCookie());
			postReq.setHeader("Referer", wpClient.getReferer());
			response = client.execute(postReq);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("connect failed because unexpected status code!");
				throw new ConnectException();
			}
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Score Page:");
			logger.debug(pageContent);
			// 按需求解析html<td>标签内容并输出
			// for (int i = 0; i < 7; i++) {
			// System.out.println(eleGrade.get(i).text());
			// }
			//
			// for (int i = 11; i < eleGrade.size(); i = i + 10) {
			// if (i + 15 < eleGrade.size()) {
			// System.out.print(eleGrade.get(i).text() + "       ");
			// i = i + 5;
			// System.out.print(eleGrade.get(i).text());
			// System.out.println();
			// }
			// }
			return pageContent;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorParseException();
		}
	}

	/**
	 * 课程表查询
	 * 
	 * @param wpClient
	 *            WPClient
	 * @param grade
	 *            学年
	 * @param term
	 *            学期
	 * @throws ConnectException
	 *             页面访问失败
	 * @return 课表页面的HTML内容
	 */
	public static String queryClassSheet(WPClient wpClient, String grade,
			String term) throws ConnectException {
		HttpClient client = wpClient.getHttpClient();
		HttpResponse response = null;
		HttpPost postReq = null;
		String pageContent = null;
		String url = null;
		try {
			url = getUserURL(wpClient, ConfigName.PAGE_XSKBXC); // 获取URL
			postReq = new HttpPost(url);
			// postReq.setHeader("Cookie", wpClient.getCookie());
			postReq.setHeader("Referer", wpClient.getReferer());
			response = client.execute(postReq);
			pageContent = PageParseUtil.getHTMLContent(response);
			logger.debug("Class Sheet Query Page Content:");
			logger.debug(pageContent);
			// 封装请求参数
			String viewState = PageParseUtil.getParam__VIEWSTATE(pageContent);
			List<NameValuePair> queryClassSheetPair = new ArrayList<NameValuePair>();
			queryClassSheetPair.add(new BasicNameValuePair("__EVENTTARGET",
					"xnd"));
			queryClassSheetPair.add(new BasicNameValuePair("__EVENTARGUMENT",
					""));
			queryClassSheetPair.add(new BasicNameValuePair("__VIEWSTATE",
					viewState));
			queryClassSheetPair.add(new BasicNameValuePair("xnd", grade));// 学年
			queryClassSheetPair.add(new BasicNameValuePair("xqd", term));// 学期
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
					queryClassSheetPair);
			// postReq.setHeader("Cookie", wpClient.getCookie());
			postReq.setEntity(entity); // 设置查询参数
			postReq.setHeader("Referer", wpClient.getReferer());
			response = client.execute(postReq);
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
			throw new ConnectException();
		}
	}

	/**
	 * 注销登录
	 * 
	 * @param wpClient
	 */
	public static void logout(WPClient wpClient) {
	}

}
