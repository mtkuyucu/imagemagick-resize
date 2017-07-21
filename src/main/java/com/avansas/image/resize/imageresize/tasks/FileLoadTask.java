package com.avansas.image.resize.imageresize.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mehmet Tayyar Kuyucu - (bluenesman)
 * @on 21/07/2017 - 17:47
 */
@Component
public class FileLoadTask {
    private static final Logger log = LoggerFactory.getLogger(FileLoadTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    @Scheduled(cron ="0 0/2 * * * *")
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}
