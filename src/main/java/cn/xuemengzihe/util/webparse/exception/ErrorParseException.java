package cn.xuemengzihe.util.webparse.exception;

/**
 * <h1>解析异常</h1>
 * <p>
 * </p>
 * 
 * @author 李春
 * @time 2017年3月12日 下午1:57:57
 */
public class ErrorParseException extends Exception {
	private static final long serialVersionUID = 1L;

	@Override
	public void printStackTrace() {
		System.err.println("Information of this exception: ");
		System.err.println("Project: Web Parse");
		System.err.println("Cause: parse error ");
		super.printStackTrace();
	}
}
