package com.renova.imagetools.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ImageResizeScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ImageResizeScheduler.class);
    @Autowired
    private ImageResizeJob imageResizeJob;

    @Scheduled(cron = "${imageresizer.cron.schedule}")
    public void resizeImages() {
        LOG.info("Image resize operation started");
        try {
            imageResizeJob.perform();
        } catch (Exception e) {
            LOG.info("Image Resize Job Throw Error. err : {}", e.getStackTrace());
        }
        LOG.info("Image resize operation finished");
    }

}
