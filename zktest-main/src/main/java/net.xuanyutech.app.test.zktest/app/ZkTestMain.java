/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * zktest app entrance
 */
@Slf4j
@SpringBootApplication(scanBasePackages="net.xuanyutech.app.test.zktest")
public class ZkTestMain {

  public static void main(String[] args) {
    SpringApplication.run(ZkTestMain.class,args);
  }

}
