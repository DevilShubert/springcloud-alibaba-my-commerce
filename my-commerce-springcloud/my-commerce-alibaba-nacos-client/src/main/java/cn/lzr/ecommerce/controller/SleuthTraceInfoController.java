package cn.lzr.ecommerce.controller;

import cn.lzr.ecommerce.service.SleuthTraceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sleuth")
public class SleuthTraceInfoController {
    final SleuthTraceInfoService sleuthTraceInfoService;

    public SleuthTraceInfoController(SleuthTraceInfoService sleuthTraceInfoService) {
        this.sleuthTraceInfoService = sleuthTraceInfoService;
    }

    /**
     * <h2>打印当前跟踪信息到日志中</h2>
     */
    @GetMapping("/trace-info")
    public void logCurrentTraceInfo(){
        sleuthTraceInfoService.logCurrentTraceInfo();
    }
}
