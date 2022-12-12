package cn.lzr.ecommerce.service;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SleuthTraceInfoService {
    /**brave.Tracer 跟踪对象**/
    private final Tracer tracer;

    public SleuthTraceInfoService(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * <h2>打印当前跟踪信息到日志中</h2>
     */
    public void logCurrentTraceInfo(){
        log.info("Sleuth trace id: [{}]", tracer.currentSpan().context().traceId());
        log.info("Sleuth span id: [{}]", tracer.currentSpan().context().spanId());
    }

}

