package net.xuanyutech.app.test.zktest.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.xuanyutech.app.test.zktest.app.ZkTestConfig;
import net.xuanyutech.app.test.zktest.app.ZkTestConstant;
import net.xuanyutech.app.test.zktest.exception.NonConnectedException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ZooKeeper实例封装
 * <b>注：非线程安全，不可多线程操作单个实例。</b></b>
 */
@Slf4j
@Component
@Scope("prototype")
public class ZooKeeperClient {

    @Getter
    private String name ;

    @Getter
    private String zkServerClusterHosts ;

    private static  AtomicInteger clientCount = new AtomicInteger(1) ;

    @Getter
    private  int  clientIndex ;

    private ZooKeeper zooKeeper;

    @Autowired
    ZooKeeperClientReadyWatcher zooKeeperClientReadyWatcher ;

    @Autowired
    ZkTestConfig zkTestConfig ;

    public ZooKeeperClient() {
        clientIndex = clientCount.getAndIncrement();
    }

    /**
     * 建立zookeeper连接
     * @return ZooKeeperClient
     * @throws IOException
     * @throws InterruptedException
     */
    public  ZooKeeperClient connect(String zkServerClusterHosts) throws IOException, InterruptedException{

        if(isConnected()){
            close();
        }

        log.debug("准备连接zookeeper服务器,[{}].",zkServerClusterHosts);
        this.zooKeeper = new ZooKeeper(zkServerClusterHosts, zkTestConfig.getClientConnectionTimeout(), zooKeeperClientReadyWatcher);

        zooKeeperClientReadyWatcher.waitClientReady();
        this.zkServerClusterHosts=zkServerClusterHosts;
        this.name=String.format("%d-%s",clientIndex,zkServerClusterHosts);
        //ensureZooKeeperServerRootPath();
        log.info("连接zookeeper服务器,[{}]成功.",zkServerClusterHosts);
        return this;
    }

    /**
     * 关闭zookeeper链接
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
            log.info("关闭zookeeperclient[{}] 成功.",name);
            this.zooKeeper = null ;
            this.zkServerClusterHosts=null;
            this.name=null;
        }
    }

    /**
     * 创建节点
     *
     * @param nodePath 节点path
     * @param nodeData 节点数据
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    public String createNode(String nodePath, String nodeData , List<ACL> acl , CreateMode mode)
        throws InterruptedException, KeeperException, IOException {
        ensureConnect();
        String str = zooKeeper.create(nodePath, nodeData == null? null : nodeData.getBytes(), acl,mode);
        log.debug("创建节点成功,[{}]" ,nodePath);
        return str;
    }

    /**
     * 创建开放持久节点
     *
     * @param nodePath 节点path
     * @param nodeData 节点数据
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    public String createPublicPersistentNode(String nodePath, String nodeData )
        throws InterruptedException, KeeperException, IOException {
        return createNode(nodePath,nodeData,ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
    }

    /**
     * 递归创建开放持久节点
     *
     * @param node 节点
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void createPublicPersistentNodeRecursive(ZooKeeperNode node )
        throws InterruptedException, KeeperException, IOException {
        if(node == null || StringUtils.isEmpty(node.getPath()))
          return ;
        if(existNode(node.getPath())==null){
            createPublicPersistentNode(node.getPath(),null);
        }
        List<ZooKeeperNode> childNodeList = node.getChildNodeList();

        if( childNodeList == null || childNodeList.size() < 1 ) {
          return;
        }
        for(ZooKeeperNode childNode : childNodeList){
          createPublicPersistentNodeRecursive(childNode);
        }
      log.debug("创建节点及子节点成功,node:[{}]." ,node.getPath());
    }

    /**
     * 更新节点数据
     *
     * @param nodePath 节点path
     * @param nodeData 节点数据
     * @return
     * @throws InterruptedException
     * @throws KeeperException
     */
    public Stat updateNodeData(String nodePath, String nodeData)
        throws InterruptedException, KeeperException, IOException {
        ensureConnect();
        Stat stat = zooKeeper.setData(nodePath, nodeData == null? null : nodeData.getBytes(), -1);
        log.debug("更新节点数据成功,node:[{}],data:[{}]" ,nodePath,nodeData);
        return stat;
    }

    /**
     * 获得节点数据
     *
     * @param nodePath 节点path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String getNodeData(String nodePath)
        throws KeeperException, InterruptedException, IOException {
        ensureConnect();
        byte[] data = zooKeeper.getData(nodePath, false, null);
        log.debug("获取节点数据成功,node:[{}],data:[{}]" ,nodePath,data);
        return data == null ? null:new String(data);
    }


    /**
     * 获取当前节点的子节点(不包含孙子节点)
     *
     * @param nodePath 父节点path
     * @return 如果没有子节点，将返回null.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public ZooKeeperNode getNodeRecursive(String nodePath)
        throws KeeperException, InterruptedException, IOException {
        ensureConnect();
        ZooKeeperNode node ;
        ZooKeeperNode childNode ;
        if(existNode(nodePath)==null) {
            return null;
        }
        node = new ZooKeeperNode() ;
        node.setPath(nodePath);
        List<String> children  = zooKeeper.getChildren(nodePath, false);

        if(children != null && children.size()>0) {
            List<ZooKeeperNode> childNodeList = new ArrayList<>(children.size());
            node.setChildNodeList(childNodeList);

            for (String childNodePath : children) {
                childNode = getNodeRecursive(nodePath+ZkTestConstant.ZOOKEEPER_PATH_SEPERATOR+childNodePath);
                if (childNode != null) {
                    childNodeList.add(childNode);
                }
            }
        }
        log.debug("获取节点成功,node:[{}],childNodeString:[{}]" ,nodePath,children);
        return node;
    }

    /**
     * 获取当前节点的子节点(不包含孙子节点)
     *
     * @param nodePath 父节点path
     * @return 如果没有子节点，将返回null.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<ZooKeeperNode> getChildNodedRecursive(String nodePath)
        throws KeeperException, InterruptedException, IOException {
        ZooKeeperNode node =  getNodeRecursive(nodePath) ;
        return  node == null ? null : node.getChildNodeList();
    }

    /**
     * 获取当前节点的子节点(不包含孙子节点)
     *
     * @param nodePath 父节点path
     * @return 如果没有子节点，将返回null.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> getChildNodeString(String nodePath)
        throws KeeperException, InterruptedException, IOException {
        ensureConnect();
        List<String> children  = zooKeeper.getChildren(nodePath, false);

        if(children == null || children.size()==0) {
            log.debug("获取节点子节点成功,node:[{}],没有子节点." ,nodePath);
            return null;
        }

        log.debug("获取节点子节点成功,node:[{}],childNodeList:[{}]" ,nodePath,children);
        return children;
    }


    /**
     * 判断节点是否存在
     *
     * @param nodePath
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat existNode(String nodePath) throws KeeperException, InterruptedException, IOException {
        ensureConnect();
        Stat stat = zooKeeper.exists(nodePath, false);
        log.debug("判断节点是否存,node:[{}],exist:[{}]" ,nodePath,stat);
        return stat;
    }

    /**
     * 删除指定节点（只能是叶子节点）
     *
     * @param nodePath
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void deleteNode(String nodePath)
        throws InterruptedException, KeeperException, IOException {
        ensureConnect();
        if(existNode(nodePath) == null){
            return;
        }
        zooKeeper.delete(nodePath, -1);
        log.debug("删除节点成功,node:[{}]," ,nodePath);
    }

    /**
     * 删除指定路径下的所有节点(包括所有子节点）
     * 递归删除 因为zookeeper只允许删除叶子节点，如果要删除非叶子节点，只能使用递归
     * @param nodePath
     * @throws Exception
     */
    public void deleteNodeRecursive(String nodePath)
        throws KeeperException, InterruptedException, IOException {
        ensureConnect();
        if(existNode(nodePath) == null){
            return;
        }
        //获取路径下的节点
        String fullChildNodePath = "";
        List<String> children = zooKeeper.getChildren(nodePath, false);
        for (String childNodePath : children) {
            //获取父节点下面的子节点路径
            fullChildNodePath = nodePath+ZkTestConstant.ZOOKEEPER_PATH_SEPERATOR+childNodePath;
            //递归调用,判断是否是根节点
            deleteNodeRecursive(fullChildNodePath);
        }
        //删除节点,并过滤zookeeper节点和 /节点
        if (nodePath != null && !nodePath.trim().startsWith("/zookeeper") && !nodePath.trim().equals("/")) {
            zooKeeper.delete(nodePath, -1);
            //打印删除的节点路径
            log.debug("删除节点成功,node:[{}]," ,nodePath);
        }
    }

    /**
     * 确保已经连接到服务器
     * 主要针对（网络抖动问题）
     * @throws IOException
     * @throws InterruptedException
     */
    private void ensureConnect() throws IOException, InterruptedException {

        //首次尝试重连
        if(!isConnected())
        {
            //缺少关键参数，异常
            if(StringUtils.isEmpty(this.zkServerClusterHosts)) {
                throw new NonConnectedException();
            }
            log.warn("[{}]连接丢失，尝试重新连接.",this.name);
            connect(this.zkServerClusterHosts);

            //再次确认
            if(!isConnected()) {
                throw new NonConnectedException();
            }
        }
    }

    /**
     * 是否已经连接到服务器
     */
    public boolean isConnected(){
        return this.zooKeeper != null && this.zooKeeper.getState().isConnected() ;
    }

}
