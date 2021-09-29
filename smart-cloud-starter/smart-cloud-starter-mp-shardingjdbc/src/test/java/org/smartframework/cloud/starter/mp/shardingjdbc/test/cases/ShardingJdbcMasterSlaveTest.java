package org.smartframework.cloud.starter.mp.shardingjdbc.test.cases;

import org.apache.shardingsphere.infra.hint.HintManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.smartframework.cloud.starter.mp.shardingjdbc.test.prepare.shardingjdbcmasterslave.ShardingJdbcMasterSlaveApp;
import org.smartframework.cloud.starter.mp.shardingjdbc.test.prepare.shardingjdbcmasterslave.biz.OrderBillBiz;
import org.smartframework.cloud.starter.mp.shardingjdbc.test.prepare.shardingjdbcmasterslave.entity.OrderBillEntity;
import org.smartframework.cloud.utility.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * sharding主从数据源集成测试
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ShardingJdbcMasterSlaveApp.class, args = "--spring.profiles.active=shardingjdbcmasterslave")
public class ShardingJdbcMasterSlaveTest {

    @Autowired
    private OrderBillBiz orderBillBiz;

    @Test
    void testShardingJdbcMasterSlaveOrder() {
        OrderBillEntity orderBillEntity = orderBillBiz.buildEntity();
        orderBillEntity.setOrderNo(RandomUtil.generateRandom(false, 32));
        orderBillEntity.setAmount(100L);
        orderBillEntity.setStatus((byte) 1);
        orderBillEntity.setPayState((byte) 1);
        orderBillEntity.setBuyer(1L);
        orderBillEntity.setInsertUser(1L);
        boolean saveResult = orderBillBiz.save(orderBillEntity);
        Assertions.assertThat(saveResult).isTrue();

        // 默认查从库
        OrderBillEntity slaveEntity = orderBillBiz.getById(orderBillEntity.getId());
        Assertions.assertThat(slaveEntity).isNull();

        // 强制查主库
        try (HintManager hintManager = HintManager.getInstance();) {
            hintManager.setWriteRouteOnly();
            OrderBillEntity masterEntity = orderBillBiz.getById(orderBillEntity.getId());
            Assertions.assertThat(masterEntity).isNotNull();
        }
    }

}