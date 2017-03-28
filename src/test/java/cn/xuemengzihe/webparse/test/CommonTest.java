package cn.xuemengzihe.webparse.test;

import org.junit.Test;

import cn.xuemengzihe.util.webparse.WPClient;
import cn.xuemengzihe.util.webparse.exception.ConnectException;
import cn.xuemengzihe.util.webparse.exception.ErrorParseException;
import cn.xuemengzihe.util.webparse.parse.PageOperateUtil;
import cn.xuemengzihe.util.webparse.parse.PageParseUtil;

public class CommonTest {
	@Test
	public void test() throws ErrorParseException {
		WPClient client = new WPClient();
		client.setUserSNO("1303050422");
		client.setUserName("李春");
		client.setPassword("140222199501057517");

		client.login();
		System.out
				.println("***********************************************************");
		System.out.println(client);
		String temp = PageOperateUtil.queryScore(client, "1", "1");
		System.out.println(PageParseUtil.getScoreResult(temp));
		System.out.println(client);
		client.logout();
	}

	@Test
	public void test2() throws ConnectException {
		WPClient client = new WPClient();
		client.setUserSNO("1303050422");
		client.setUserName("李春");
		client.setPassword("140222199501057517");

		client.login();
		System.out
				.println("***********************************************************");
		System.out.println(client);
		PageOperateUtil.queryClassSheet(client, "2013-2014", "1");
		System.out.println(client);
		client.logout();
	}
}
