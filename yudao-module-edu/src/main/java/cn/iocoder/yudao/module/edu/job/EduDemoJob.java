package cn.iocoder.yudao.module.edu.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EduDemoJob implements JobHandler {

    @Override
    public String execute(String param) throws Exception {
        log.info("这是一个JobDemo");
        return "这是一个JobDemo";
    }
}
