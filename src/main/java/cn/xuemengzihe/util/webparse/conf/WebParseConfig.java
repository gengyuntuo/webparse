package cn.xuemengzihe.util.webparse.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>获取WebPrase工程的配置信息</h1>
 * <p>
 * WebParse的配置信息存放在classpath下的webparse.cfg.properties文件中
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 上午10:23:12
 */
public class WebParseConfig {
	private static Logger logger = LoggerFactory
			.getLogger(WebParseConfig.class);
	/**
	 * 配置文件路劲及名称
	 */
	private static String confFileName = "webparse.cfg.properties";
	/**
	 * 属性
	 */
	private static Properties props;

	/**
	 * 初始化配置信息
	 */
	private static synchronized void initConfig() {
		props = new Properties();
		InputStream in = WebParseConfig.class.getClassLoader()
				.getResourceAsStream(confFileName);
		try {
			if (in == null) {
				logger.error("WebParse Project: Can't find the config file "
						+ confFileName);
				throw new IOException();
			}
			props.load(in);
		} catch (IOException e) {
			logger.error("WebParse Porject: Config loaded failed!");
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 重新加载配置文件
	 */
	public static void reloadConfig() {
		initConfig();
	}

	/**
	 * 获取配置信息，其参数参见{@link ConfigName}中定义的常量
	 * 
	 * @param key
	 * @return
	 */
	public static String getConfig(String key) {
		if (props == null) {
			initConfig();
		}
		String result = props.getProperty(key);
		if (result == null) {
			logger.warn("WebParse Project: can't use the " + key
					+ " obtain a value from config file!");
		}
		logger.debug("WebParse Project: Get a parameter from config, it's [key:"
				+ key + "->" + result + "]");
		return result;
	}
}
