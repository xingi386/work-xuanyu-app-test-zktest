/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.xuanyutech.app.test.zktest.util.ZooKeeperClient;
import net.xuanyutech.app.test.zktest.util.ZooKeeperNode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Title: ZkTestTask
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 17:13   
 * @version V1.0
 */
@Slf4j
@Component
@Scope("prototype")
public class ZkTestTask  implements Runnable {

  @Getter
  private String name ;

  @Getter
  private int executeTimes = 0;
  @Getter
  private int errorTimes= 0 ;
  @Getter
  private long maxExecuteTime=0;
  @Getter
  private long maxConnectToServerTime=0L;
  @Getter
  private long maxGetNodeRecursiveTime=0L;
  @Getter
  private long maxCreateNodeRecursiveTime=0L;
  @Getter
  private long maxDeleteNodeRecursiveTime=0L;
  @Getter
  private long maxCloseConnectionTime=0L;
  @Getter
  private Date lastExecuteTime = new Date();

  private ZooKeeperNode targetNode;

  @Autowired
  private ZkTestConfig zkTestConfig;

  @Autowired
  private ZooKeeperClient zooKeeperClient ;

  private  static  int taskCount =0;

  ZkTestTask(){
    this.name = String.format("[%s-%d]",ZkTestTask.class.getSimpleName(),taskCount++ );
  }

  @Override
  public void run() {

    long operationTime =0L;
    long lastExecuteBeginTime = System.currentTimeMillis();
    lastExecuteTime.setTime(lastExecuteBeginTime);
    long lastExecuteEndTime = 0L ;

    try {

      operationTime = calcOperationTime(()->{ zooKeeperClient.connect(zkTestConfig.getServerClusterHosts()); });
      if(operationTime>maxConnectToServerTime){
        maxConnectToServerTime = operationTime;
      }
      log.debug("{}与zookeeper建立链接花费{}毫秒.",this.name,operationTime);


      operationTime = calcOperationTime(()->{targetNode = zooKeeperClient.getNodeRecursive(zkTestConfig.getAppBasePath()); });
      if(operationTime>maxGetNodeRecursiveTime){
        maxGetNodeRecursiveTime = operationTime;
      }
      log.debug("{}获取路径[{}]所有节点数据花费{}毫秒.",this.name,zkTestConfig.getAppBasePath(),operationTime);

      operationTime = calcOperationTime(()->{ zooKeeperClient.deleteNodeRecursive(targetNode.getPath()); });
      if(operationTime>maxDeleteNodeRecursiveTime){
        maxDeleteNodeRecursiveTime = operationTime;
      }
      log.debug("{}删除路径[{}]所有节点数据花费{}毫秒.",this.name,targetNode.getPath(),operationTime);

      operationTime = calcOperationTime(()->{zooKeeperClient.createPublicPersistentNodeRecursive(targetNode); });
      if(operationTime>maxCreateNodeRecursiveTime){
        maxCreateNodeRecursiveTime = operationTime;
      }
      log.debug("{} 创建路径[{}]所有节点数据花费{}毫秒.",this.name,targetNode.getPath(),operationTime);

    } catch (Exception e) {
      log.error("执行任务出错,{}",e.getMessage(),e);
      errorTimes++ ;
    }finally {

      try {
        operationTime = calcOperationTime(()->{zooKeeperClient.close();});
        if(operationTime>maxCloseConnectionTime){
          maxCloseConnectionTime = operationTime;
        }
        log.debug("{} 关闭连接花费{}毫秒.",this.name,operationTime);
      } catch (Exception e) {
        log.error("{}关闭链接出错",this.name,e);
      }

      lastExecuteEndTime = System.currentTimeMillis();
      if(lastExecuteEndTime-lastExecuteBeginTime>maxExecuteTime){
        maxExecuteTime = lastExecuteEndTime-lastExecuteBeginTime;
      }
      executeTimes++;

      if(executeTimes%10==0){
        log.info(this.toString());
      }
    }
  }


  interface Operation{
    void doOperation() throws IOException, InterruptedException, KeeperException  ;
  }

  private long calcOperationTime(Operation op) throws IOException, InterruptedException, KeeperException{
    long operationBeginTimeMillis;
    long operationEndTimeMillis;
    long operationTime ;

    operationBeginTimeMillis = System.currentTimeMillis();
    op.doOperation();
    operationEndTimeMillis = System.currentTimeMillis();

    operationTime = operationEndTimeMillis-operationBeginTimeMillis;
    return operationTime;
  }

  @Override
  public String toString() {
    return JSON.toJSONStringWithDateFormat(this,"YYYY-MM-dd HH:mm:ss.SSS",
        SerializerFeature.PrettyFormat,SerializerFeature.UseSingleQuotes);
  }
}
