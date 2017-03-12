package cn.xuemengzihe.util.webparse.conf;

/**
 * <h1>配置名称</h1>
 * <p>
 * 配置名称的常量集合，为了规范取值名称和防止开发中输入错误
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 上午11:15:43
 */
public interface ConfigName {
	/**
	 * 教学网编码
	 */
	public static final String PAGE_ENCODING = "page.encoding";
	/**
	 * 教学网主页
	 */
	public static final String WEB_URL = "web.url";
	/**
	 * 学生课表查询
	 */
	public static final String PAGE_XSKBXC = "page.xskbcx";
	/**
	 * 学生成绩查询
	 */
	public static final String PAGE_XSCJCX = "page.xscjcx";
	/**
	 * 注销登录页面
	 */
	public static final String PAGE_LOGOUT = "page.logout";
}
