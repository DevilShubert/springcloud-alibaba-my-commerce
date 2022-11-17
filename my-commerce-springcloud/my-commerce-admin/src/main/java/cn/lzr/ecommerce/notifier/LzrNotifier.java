package cn.lzr.ecommerce.notifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * <h1>自定义警告</h1>
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class LzrNotifier extends AbstractEventNotifier {
    protected LzrNotifier(InstanceRepository repository) {
        super(repository);
    }

    /**
     *
     * @param event Abstract Event regarding registered instances
     * @param instance The aggregate representing a registered application instance
     * @return
     */
    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        // 类似前端回调函数，事件形式触发
        return Mono.fromRunnable(()->{

            // 客户端实例状态发生改变事件
            if (event instanceof InstanceStatusChangedEvent) {
                // instance Name，instance Information， instance Status
                log.info("Instance Status Change: [{}], [{}], [{}]",
                        instance.getRegistration().getName(), event.getInstance(),
                        ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());
            } else {
                // 其他事件
                log.info("Instance Info: [{}], [{}], [{}]",
                        instance.getRegistration().getName(), event.getInstance(),
                        event.getType());
            }
        });
    }
}
