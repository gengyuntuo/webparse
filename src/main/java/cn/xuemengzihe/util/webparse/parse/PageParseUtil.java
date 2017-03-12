package cn.xuemengzihe.util.webparse.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xuemengzihe.util.webparse.conf.ConfigName;
import cn.xuemengzihe.util.webparse.conf.WebParseConfig;
import cn.xuemengzihe.util.webparse.exception.ErrorParseException;

/**
 * 
 * <h1>页面内容解析</h1>
 * <p>
 * 该类中提供了一些对页面内容解析的方法
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 上午11:55:47
 */
public class PageParseUtil {
	private static Logger logger = LoggerFactory.getLogger(PageParseUtil.class);

	/**
	 * 根据HttpResponse对象获取其中的HTML内容
	 * 
	 * @param response
	 *            HttpResponse
	 * @return 页面内容
	 * @throws ErrorParseException
	 *             页面内容获取失败！
	 */
	public static String getHTMLContent(HttpResponse response)
			throws ErrorParseException {
		String encoding = WebParseConfig.getConfig(ConfigName.PAGE_ENCODING); // 获取页面的编码格式
		InputStream inputStream = null;
		try {
			inputStream = response.getEntity().getContent();
			int len = 0;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			while ((len = inputStream.read(buffer)) != -1) {
				byteArray.write(buffer, 0, len);
			}
			return new String(byteArray.toByteArray(), encoding);
		} catch (Exception e) {
			logger.error("WebParse Project:PageParseUtil can't parse web content!");
			e.printStackTrace();
			throw new ErrorParseException();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 从页面内容中获取__VIEWSTATE参数
	 * 
	 * @param content
	 *            页面内容
	 * @return __VIEWSTATE的值
	 */
	public static String getParam__VIEWSTATE(String content) {
		String viewState = Jsoup.parse(content)
				.select("input[name=__VIEWSTATE]").val(); // Jsoup解析
		logger.debug("__VIEWSTATE->" + viewState);
		return viewState;
	}
}
