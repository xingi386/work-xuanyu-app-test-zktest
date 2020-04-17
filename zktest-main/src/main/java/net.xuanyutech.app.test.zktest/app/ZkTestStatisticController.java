/**
 * 深圳玄羽科技有限公司 http://www.xuanyutech.net/ Copyright ©2020 XuanYuTech Inc. All rights reserved.
 */

package net.xuanyutech.app.test.zktest.app;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Title: ZkTestStatisticController
 * @Description:
 * @author: 玄羽1107
 * @create: 2020/4/16 19:46   
 * @version V1.0
 */
@RestController
public class ZkTestStatisticController {

  @Autowired
  ZkTestApp zkTestApp ;

  @RequestMapping("/")
  public String getTestStatistic()
  {
    return JSON.toJSONString(zkTestApp.getTaskList().toString()) ;
  }


}
