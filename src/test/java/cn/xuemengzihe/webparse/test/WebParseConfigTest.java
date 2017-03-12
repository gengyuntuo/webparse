package cn.xuemengzihe.webparse.test;

import org.junit.Test;

import cn.xuemengzihe.util.webparse.conf.ConfigName;
import cn.xuemengzihe.util.webparse.conf.WebParseConfig;

public class WebParseConfigTest {

	@Test
	public void test() {
		System.out.println(WebParseConfig.getConfig(ConfigName.WEB_URL));
	}
}
