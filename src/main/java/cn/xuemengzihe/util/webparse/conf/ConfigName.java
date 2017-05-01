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
	public static final String PAGE_XSKBCX = "page.xskbcx";
	/**
	 * 学生成绩查询
	 */
	public static final String PAGE_XSCJCX = "page.xscjcx";
	/**
	 * 注销登录页面
	 */
	public static final String PAGE_LOGOUT = "page.logout";

	public static final String BTN_学期成绩 = "btn_xq";
	public static final String BTN_学年成绩 = "btn_xn";
	public static final String BTN_历年成绩 = "btn_zcj";
	public static final String BTN_课程最高成绩 = "btn_zg";
	public static final String BTN_未通过成绩 = "Button2";
	public static final String BTN_成绩统计 = "Button1";
}
