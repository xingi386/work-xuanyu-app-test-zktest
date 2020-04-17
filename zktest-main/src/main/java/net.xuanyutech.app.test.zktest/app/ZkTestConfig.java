/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.app;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Title: ZkTestConfig
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 14:13   
 * @version V1.0
 */
@Component
@Data
@PropertySource(value = "classpath:zktest.properties" , encoding = "utf-8")
public class ZkTestConfig {

  @Value("${zktest.app.basePath:/cims}")
  private String appBasePath ;

  @Value("${zktest.client.connection.timeout:5000}")
  private int clientConnectionTimeout;

  @Value("${zktest.server.cluster.host:192.168.11.31:2181,192.168.11.33:2181,192.168.11.18:2181}")
  private  String serverClusterHosts ;

  @Value("${zktest.task.parallelCount:5}")
  private int taskParallelCount;

  @Value("${zktest.task.threadpool.size:1}")
  private int taskThreadPoolSize;

  @Value("${zktest.task.scheduledPeriod:10}")
  private int taskScheduledPeriod;

}
