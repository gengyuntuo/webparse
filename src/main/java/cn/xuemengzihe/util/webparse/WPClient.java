package cn.xuemengzihe.util.webparse;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xuemengzihe.util.webparse.exception.ConnectException;
import cn.xuemengzihe.util.webparse.parse.PageOperateUtil;

/**
 * <h1>WebParse 客户端</h1>
 * <p>
 * 通过该类可以实现对教学网的访问
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 下午12:03:14
 */
public class WPClient {
	private Logger logger = LoggerFactory.getLogger(WPClient.class);
	/**
	 * 客户端状态信息:在线
	 */
	public static final String USER_STATE_ONLINE = "online";
	/**
	 * 客户端状态信息:注销
	 */
	public static final String USER_STATE_LOGOUT = "logout";
	/**
	 * 客户端状态信息:连接异常
	 */
	public static final String USER_STATE_CONERR = "connect error!";
	private String userSNO;
	private String userName;
	private String password;
	private String userState; // 用户状态
	private String cookie;
	private String referer;
	private String subUrl;
	private CloseableHttpClient httpClient;

	/**
	 * 初始化客户端
	 */
	private void initClient() {
		httpClient = HttpClients.createDefault(); // 创建客户端
	}

	public WPClient() {
		this.initClient();
	}

	public WPClient(String userSNO, String password) {
		super();
		this.userSNO = userSNO;
		this.password = password;
	}

	public String getUserState() {
		return userState;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void set(String userName, String password) {
		this.userSNO = userName;
		this.password = password;
	}

	public String getUserSNO() {
		return userSNO;
	}

	public void setUserSNO(String userSNO) {
		this.userSNO = userSNO;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getSubUrl() {
		return subUrl;
	}

	public void setSubUrl(String subUrl) {
		this.subUrl = subUrl;
	}

	public void login() {
		try {
			PageOperateUtil.login(this, userSNO, password);
			this.userState = USER_STATE_ONLINE;
		} catch (ConnectException e) {
			this.userState = USER_STATE_CONERR;
		}
	}

	/**
	 * 注销登录，请在销毁该类前调用本方法
	 */
	public void logout() {
		PageOperateUtil.logout(this);
		try {
			this.httpClient.close();
			this.userState = "logout";
		} catch (IOException e) {
		}
		logger.info("WPClient: Logout!");
	}

	@Override
	public String toString() {
		return "WPClient [logger=" + logger + ", userSNO=" + userSNO
				+ ", userName=" + userName + ", password=" + password
				+ ", userState=" + userState + ", cookie=" + cookie
				+ ", referer=" + referer + ", subUrl=" + subUrl
				+ ", httpClient=" + httpClient + "]";
	}

}
