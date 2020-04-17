/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.xuanyutech.app.test.zktest.exception.InitializationException;
import net.xuanyutech.app.test.zktest.util.ZooKeeperClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Title: ZkTestApp
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 13:40   
 * @version V1.0
 */
@Component
@Slf4j
public class ZkTestApp  {

  @Autowired
  private ZkTestConfig zkTestConfig ;
  @Autowired
  private ApplicationContext applicationContext ;

  @Autowired
  private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

  @Getter
  private List<ZkTestTask> taskList ;

  @PostConstruct
  public void init (){
    printConfiguration();
    ensureBasePath();
    initZkTestTask();
    start();
  }

  public void initZkTestTask(){
    taskList = new ArrayList<>();
    int parallelCount = zkTestConfig.getTaskParallelCount() ;
    ZkTestTask task ;
    for(int i = 0 ; i < parallelCount ; i ++){
        task = applicationContext.getBean("zkTestTask", ZkTestTask.class);
        taskList.add(task);
    }
  }

  private void printConfiguration(){
    log.debug("load zktest config:");
    log.debug("app base path:[{}]",zkTestConfig.getAppBasePath());
    log.debug("client parallel count:[{}]",zkTestConfig.getTaskParallelCount());
    log.debug("client connection timeout:[{}]",zkTestConfig.getClientConnectionTimeout());
    log.debug("server cluster host:[{}]",zkTestConfig.getServerClusterHosts());
  }


  private void ensureBasePath()  {

    ZooKeeperClient zooKeeperClient = applicationContext.getBean("zooKeeperClient", ZooKeeperClient.class) ;

    String basePath = zkTestConfig.getAppBasePath() ;

    try {
      zooKeeperClient.connect(zkTestConfig.getServerClusterHosts());

      if(zooKeeperClient.existNode(basePath)==null){
        zooKeeperClient.createPublicPersistentNode(basePath,null);
        log.info("ZooKeeperServer[{}]缺少根目录，创建根目录[{}]成功.",zooKeeperClient.getName(),basePath);
      }
    }catch (Exception e){
      log.error("ZooKeeperServer[{}]缺少根目录,创建根目录[{}]失败,将关闭zookeeper链接.",zooKeeperClient.getName(),basePath,e);
      throw new InitializationException(e);
    }finally {
      try {
        zooKeeperClient.close();
      } catch (InterruptedException e) {
        log.error("关闭ZooKeeper[{}]链接失败.",zooKeeperClient.getName(),e);
      }
    }
  }

  @Bean
  private  ScheduledThreadPoolExecutor initThreadPool(){
      return new ScheduledThreadPoolExecutor(zkTestConfig.getTaskThreadPoolSize());
  }

  private  void start(){
    taskList.stream().forEach(
        (task)->{
          scheduledThreadPoolExecutor.scheduleWithFixedDelay(
              task,0,zkTestConfig.getTaskScheduledPeriod(), TimeUnit.SECONDS);
        }
    );
  }

}
