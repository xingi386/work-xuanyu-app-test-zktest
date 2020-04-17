/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.util;

import java.util.List;
import lombok.Data;

/**
 * @Title: ZooKeeperNode
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 14:13   
 * @version V1.0
 */
@Data
public class ZooKeeperNode {

  private String path ;

  private List<ZooKeeperNode> childNodeList ;

}
