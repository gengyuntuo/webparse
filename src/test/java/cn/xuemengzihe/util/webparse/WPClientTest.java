package cn.xuemengzihe.util.webparse;

import java.io.IOException;

import org.junit.Test;

import cn.xuemengzihe.util.webparse.conf.ConfigName;
import cn.xuemengzihe.util.webparse.exception.ConnectException;

public class WPClientTest {

	@Test
	public void testWPClientStringString() throws ConnectException {
		WPClient client = new WPClient("1303050422", "140222199501057517");
		System.out.println(client.queryClassSheet("2013-2014", "2"));
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testWPClientStringStringString() {
	}

	@Test
	public void testLogin() {
	}

	@Test
	public void testQueryScore() {
	}

	@Test
	public void testQueryClassSheet() throws ConnectException {
		WPClient client = new WPClient("1303050422", "140222199501057517");
		System.out.println(client.queryScore("2013-2014", "2", ConfigName.BTN_学期成绩));
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testClose() {
	}

}
