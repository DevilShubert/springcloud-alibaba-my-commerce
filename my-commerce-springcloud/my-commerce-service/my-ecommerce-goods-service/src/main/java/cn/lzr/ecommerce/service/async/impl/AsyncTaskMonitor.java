package cn.lzr.ecommerce.service.async.impl;

import cn.lzr.ecommerce.constant.AsyncTaskStatusEnum;
import cn.lzr.ecommerce.vo.AsyncTaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <h1>异步任务执行监控切面</h1>
 * */
@Slf4j
@Component
@Aspect
public class AsyncTaskMonitor {

    final
    AsyncTaskManager asyncTaskManager;

    public AsyncTaskMonitor(AsyncTaskManager asyncTaskManager) {
        this.asyncTaskManager = asyncTaskManager;
    }

    /**
     * 切入点
     */
    @Pointcut("execution(* cn.lzr.ecommerce.service.async.impl.AsyncServiceImpl.asyncImportGoods(..))")
    public void pointCut(){}

    /**
     * <h2>异步任务执行的环绕切面</h2>
     * 环绕切面让我们可以在方法执行之前和执行之后做一些 "额外" 的操作
     * */
    @Around("pointCut()")
    public Object taskHandle(ProceedingJoinPoint proceedingJoinPoint){
        // 获取 taskId, 调用异步任务传入的第二个参数
        Object[] args = proceedingJoinPoint.getArgs();
        String taskId = args[1].toString();

        // 获取任务信息, 在controller层提交任务的时候就已经放入到容器中了
        AsyncTaskInfo taskInfo = asyncTaskManager.getTaskInfo(taskId);
        log.info("AsyncTaskMonitor is monitoring async task: [{}]", taskId);

        taskInfo.setStatus(AsyncTaskStatusEnum.RUNNING);
        asyncTaskManager.setTaskInfo(taskInfo); // 设置为运行状态, 并重新放入容器

        AsyncTaskStatusEnum status;
        Object result;

        try {
            // 执行异步任务
            result = proceedingJoinPoint.proceed();
            status = AsyncTaskStatusEnum.SUCCESS;
        } catch (Throwable ex) {
            // 异步任务出现了异常
            result = null;
            status = AsyncTaskStatusEnum.FAILED;
            log.error("AsyncTaskMonitor: async task [{}] is failed, Error Info: [{}]",
                    taskId, ex.getMessage(), ex);
        }

        // 设置异步任务其他的信息, 再次重新放入到容器中
        taskInfo.setEndTime(new Date());
        taskInfo.setStatus(status);
        taskInfo.setTotalTime(String.valueOf(
                taskInfo.getEndTime().getTime() - taskInfo.getStartTime().getTime()
        ));
        asyncTaskManager.setTaskInfo(taskInfo);
        // 切面执行完毕
        return result;
    }
}
