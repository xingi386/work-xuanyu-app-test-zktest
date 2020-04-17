/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest;

import lombok.extern.slf4j.Slf4j;
import net.xuanyutech.app.test.zktest.app.ZkTestApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * zktest app entrance
 */
@Slf4j
@SpringBootApplication
public class ZkTestMain {

  public static void main(String[] args) {
    ApplicationContext applicationContext = SpringApplication.run(ZkTestMain.class,args);
    ZkTestApp zkTestApp = applicationContext.getBean("zkTestApp", ZkTestApp.class);
    zkTestApp.start();
  }

}
