package cn.lzr.ecommerce.config;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
@Component
// DynamicRouteServiceImplByNacos必须等待gatewayConfig这个bean存在之后才能注入到IOC容器中
@DependsOn({"gatewayConfig"})
public class DynamicRouteServiceImplByNacos {
    /** Nacos 配置服务，官方SDK */
    private ConfigService configService;

    @Autowired
    private final DynamicRouteServiceImpl dynamicRouteService;

    public DynamicRouteServiceImplByNacos(DynamicRouteServiceImpl dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

    /**
     * <h2>Bean 在容器中构造完成之后会执行 init 方法</h2>
     * 猜测也就是对象初始化完成，在堆中会执行的 init 方法
     * */
    @PostConstruct
    public void init(){
        log.info("gateway route init...");
        try {
            // 初始化 Nacos 配置客户端
            configService = initConfigService();
            if (Objects.isNull(configService)){
                log.error("init config service fail");
                return;
            }
            // 通过 Nacos Config 并指定路由配置路径去获取路由配置
            String configInfo = configService.getConfig(
                    GatewayConfig.NACOS_ROUTE_DATA_ID,
                    GatewayConfig.NACOS_ROUTE_GROUP,
                    GatewayConfig.DEFAULT_TIMEOUT
            );

            log.info("get current gateway config: [{}]", configInfo);
            List<RouteDefinition> definitionList = JSON.parseArray(configInfo, RouteDefinition.class);

            if (CollectionUtil.isNotEmpty(definitionList)) {
                definitionList.forEach(df -> {
                    log.info("init gateway config: [{}]", df.toString());
                    dynamicRouteService.addRouteDefinition(df);
                });
            }

            // 设置监听器
            dynamicRouteByNacosListener(GatewayConfig.NACOS_ROUTE_DATA_ID,
                    GatewayConfig.NACOS_ROUTE_GROUP);

        } catch (Exception ex) {
            log.error("gateway route init has some error: [{}]", ex.getMessage(), ex);
        }
    }

    /**
     * <h2>初始化 Nacos Config</h2>
     * */
    private ConfigService initConfigService(){
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", GatewayConfig.NACOS_SERVER_ADDR);
            properties.setProperty("namespace", GatewayConfig.NACOS_NAMESPACE);
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception ex) {
            log.error("init gateway nacos config error: [{}]", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * <h2>监听 Nacos 下发的动态路由配置</h2>
     * @param dataId
     * @param group
     */
    private void dynamicRouteByNacosListener(String dataId, String group){
        try {
            // 给 Nacos Config 客户端增加一个监听器
            configService.addListener(dataId, group, new Listener() {
                /**
                 * <h2>自己提供线程池，为这个事件执行监听操作</h2>
                 * */
                @Override
                public Executor getExecutor() {
                    return null;
                }

                /**
                 * <h2>监听器收到配置更新，也就是收到事件后，触发的回调函数</h2>
                 * @param configInfo Nacos 中最新的配置定义
                 * */
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("start to update config: [{}]", configInfo);
                    List<RouteDefinition> definitionList =
                            JSON.parseArray(configInfo, RouteDefinition.class);
                    log.info("update route: [{}]", definitionList.toString());
                    dynamicRouteService.updateList(definitionList);
                }
            });
        } catch (Exception ex) {
            log.error("dynamic update gateway config error: [{}]", ex.getMessage(), ex);
        }
    }
}
