package cn.lzr.ecommerce.service.async.impl;

import cn.lzr.ecommerce.constant.AsyncTaskStatusEnum;
import cn.lzr.ecommerce.goods.GoodsInfo;
import cn.lzr.ecommerce.vo.AsyncTaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * <h1>异步任务执行管理器</h1>
 * 对异步任务进行包装管理, 记录并塞入异步任务执行信息
 * */
@Slf4j
@Component
public class AsyncTaskManager {

    /** 异步任务执行信息容器 */
    private final HashMap<String, AsyncTaskInfo> taskContainer = new HashMap<String, AsyncTaskInfo>();

    private final AsyncServiceImpl asyncService;

    public AsyncTaskManager(AsyncServiceImpl asyncService) {
        this.asyncService = asyncService;
    }

    /**
     * <h2>初始化异步任务</h2>
     * */
    public AsyncTaskInfo initTask(){
        AsyncTaskInfo taskInfo = new AsyncTaskInfo();
        // 设置一个唯一的异步任务 id, 只要唯一即可
        taskInfo.setTaskId(UUID.randomUUID().toString());
        taskInfo.setStatus(AsyncTaskStatusEnum.STARTED);
        taskInfo.setStartTime(new Date());

        // 初始化的时候就要把异步任务执行信息放入到存储容器中
        taskContainer.put(taskInfo.getTaskId(), taskInfo);
        return taskInfo;
    }


    /**
     * <h2>提交异步任务，开始执行</h2>
     * 重点：通过@Async注解标注的异步任务方法会立即返回
     * */
    public AsyncTaskInfo submit(List<GoodsInfo> goodsInfos){
        // 初始化一个异步任务的监控信息
        AsyncTaskInfo taskInfo = initTask();
        asyncService.asyncImportGoods(goodsInfos, taskInfo.getTaskId());
        return taskInfo;
    }


    /**
     * <h2>设置异步任务执行状态信息（作用相当于更新异步任务的状态）</h2>
     * */
    public void setTaskInfo(AsyncTaskInfo taskInfo) {
        taskContainer.put(taskInfo.getTaskId(), taskInfo);
    }

    /**
     * <h2>获取异步任务执行状态信息</h2>
     * */
    public AsyncTaskInfo getTaskInfo(String taskId) {
        return taskContainer.get(taskId);
    }
}
