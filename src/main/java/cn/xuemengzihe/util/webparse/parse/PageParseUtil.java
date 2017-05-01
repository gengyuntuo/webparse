package cn.xuemengzihe.util.webparse.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

	/**
	 * 使用JSOUP解析成绩查询结果页面，将其中的成绩解析出来
	 * 
	 * @param pageContent
	 * @return 结果
	 */
	public static List<Map<String, String>> getScoreResult(String pageContent) {
		List<Map<String, String>> scoreList = null; // 所有的成绩
		Map<String, String> subjectScore = null; // 某一门课程的成绩信息
		List<String> tableColsTitle = null; // 成绩查询结果的Table的列标题
		Document page = Jsoup.parse(pageContent);
		Elements elements = page.select("#Datagrid1");
		if (elements == null) {
			throw new RuntimeException("页面内容出错！没有找到table元素");
		}
		Element element = elements.get(0);
		if (element == null) {
			throw new RuntimeException("页面内容出错！没有找到table元素");
		}

		// 初始化集合
		scoreList = new ArrayList<>();

		// 依次遍历查询table中每一行的结果
		for (Element tr : element.select("tr")) {
			if (tableColsTitle == null) { // 读取第一行的table列标题
				tableColsTitle = new ArrayList<>();
				for (Element td : tr.select("td")) {
					tableColsTitle.add(td.text().trim());
				}
				continue; // 读取完第一行，读取第二行
			}
			// 读取第二行
			int index = 0;
			subjectScore = new HashMap<>(); // 初始化科目
			for (Element td : tr.select("td")) {
				subjectScore.put(tableColsTitle.get(index++), td.text().trim());
			}
			scoreList.add(subjectScore);
		}
		return scoreList;
	}

	/**
	 * 根据登录页面获取用户名
	 * 
	 * @param content
	 * @return
	 */
	public static String getUserName(String content) {
		Document page = Jsoup.parse(content);
		// 获取包含用户名称的元素的内容（例如：<span id="xhxm">1303050422 李春同学</span>）
		String userName = page.getElementById("xhxm").text();
		userName = userName.split(" ")[1];
		userName = userName.substring(0, userName.lastIndexOf("同学"));
		return userName;
	}
}
