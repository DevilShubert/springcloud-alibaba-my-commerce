package cn.lzr.ecommerce.config;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings("all")
public class DynamicRouteServiceImpl implements ApplicationEventPublisherAware {

    /**publish用于事件发布**/
    private  ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    /**这两个routeDefinition都是在IOC容器中被管理，需要时自动注入**/
    /** 写路由定义 */
    @Autowired
    private final RouteDefinitionWriter routeDefinitionWriter;
    /** 获取路由定义 */
    @Autowired
    private final RouteDefinitionLocator routeDefinitionLocator;
    // 非static的fianl变量必须在初始化时进行实例化
    public DynamicRouteServiceImpl(RouteDefinitionWriter routeDefinitionWriter, RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }
    /**
     * <h2>增加路由定义</h2>
     */
    public String addRouteDefinition(RouteDefinition routeDefinition) {
        log.info("gateway add route: [{}]", routeDefinition);

        // 保存路由并发布(webfulx响应式编程)
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        // 发布事件通知给 Gateway, 同步新增的路由定义
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    /**
     * <h2>更新一组路由</h2>
     * 思路是先删除RouteDefinition再保存
     */
    public String updateList(List<RouteDefinition> definitions){
        log.info("gateway update route: [{}]", definitions);

        List<RouteDefinition> routeDefinitionsExits =
                routeDefinitionLocator.getRouteDefinitions().buffer().blockFirst();

        // 如果路由信息不为空，则清除掉之前的所有信息
        if (!CollectionUtils.isEmpty(routeDefinitionsExits)){
            routeDefinitionsExits.forEach(rde ->{
                log.info("delete route definition: [{}]", rde);
                deleteById(rde.getId());
            });
        }

        // 路由信息为空，直接把需要新的的路由定义同步到 gateway 中
        definitions.forEach(definition -> updateByRouteDefinition(definition));
        return "success";
    }

    /**
     * <h2>根据路由id删除路由配置</h2>
     */
    private String deleteById(String id) {
        try{
            log.info("gateway delete route id: [{}]", id);
            // 先修改RouteDefinition，再事件通知
            this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
            this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
            return "delete success";
        } catch (Exception ex) {
            log.error("gateway delete route fail: [{}]", ex.getMessage(), ex);
            return "delete fail";
        }
    }

    /**
     * <h2>更新单个路由</h2>
     */
    private String updateByRouteDefinition(RouteDefinition definition) {
        try{
            log.info("gateway update route: [{}]", definition);
            // 保险起见还是先删除
            this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
        } catch (Exception ex){
            return "update fail, not find route routeId: " + definition.getId();
        }

        try {
            this.routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            // 事件似乎是每次结束操作触发一次
            this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
            return "success";
        } catch (Exception ex) {
            return "update route fail";
        }
    }
}
