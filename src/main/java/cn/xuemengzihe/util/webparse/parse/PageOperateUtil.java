package cn.xuemengzihe.util.webparse.parse;

import java.io.IOException;
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
			nameValuePairLogin
					.add(new BasicNameValuePair("TextBox1", userSNO));// 学号
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
			}
			subUrl = response.getFirstHeader("Location").getValue();
			wpClient.setReferer(subUrl); // 设置Referrer
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

	public static void getScoreQueryPage(WPClient wpClient) {
		;
	}
	public static void scoreQuery(WPClient wpClient) {
		;
	}

	/**
	 * 注销登录
	 * 
	 * @param wpClient
	 */
	public static void logout(WPClient wpClient) {
	}

}
