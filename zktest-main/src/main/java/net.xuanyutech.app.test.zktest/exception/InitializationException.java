/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.exception;

/**
 * @Title: InitializationException
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 16:35   
 * @version V1.0
 */
public class InitializationException extends  RuntimeException{

  public InitializationException() {
    super();
  }

  public InitializationException(String message) {
    super(message);
  }

  public InitializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public InitializationException(Throwable cause) {
    super(cause);
  }

  protected InitializationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
