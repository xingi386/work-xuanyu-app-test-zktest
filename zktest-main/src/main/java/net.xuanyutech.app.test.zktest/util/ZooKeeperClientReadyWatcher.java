package net.xuanyutech.app.test.zktest.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.xuanyutech.app.test.zktest.app.ZkTestConfig;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 客户端是否已经READY的监视器
 */
@Slf4j
@Component
@Scope("prototype")
public class ZooKeeperClientReadyWatcher implements Watcher {

  @Autowired
  private ZkTestConfig zkTestConfig ;

    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public void waitClientReady() throws InterruptedException {
        connectedSignal.await(zkTestConfig.getClientConnectionTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //客户端与ZK建立连接后，Watcher的process方法会被调用，参数是表示该连接的事件，
        // 连接成功后调用CountDownLatch的countDown方法，计数器减为0，释放线程锁，zk对象可用
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

}
