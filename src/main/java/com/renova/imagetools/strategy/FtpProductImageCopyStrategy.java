package com.renova.imagetools.strategy;

import com.renova.imagetools.event.FtpFailEvent;
import com.google.common.eventbus.EventBus;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class FtpProductImageCopyStrategy implements ProductImageCopyStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(FtpProductImageCopyStrategy.class);
    private static final String CHANNEL_TYPE = "sftp";
    
    @Autowired
    private EventBus eventBus;

    @Value("${product.image.ftp.user}")
    private String ftpUser;
    @Value("${product.image.ftp.password}")
    private String ftpPassword;
    @Value("${product.image.ftp.host}")
    private String ftpHost;
    @Value("${product.image.ftp.port}")
    private int ftpPort;
    @Value("${product.image.ftp.path}")
    private String ftpPath;

    @Override
    public void copy(File file, String productCode) throws IOException {
        if (ftpPort == 21) {
            LOG.info("FTP strategy selected");
            FTPClient ftpClient = new FTPClient();
            try {
                String fileName = file.getName();
                ftpClient.connect(ftpHost);
                ftpClient.setControlKeepAliveTimeout(300);
                if (!ftpClient.login(ftpUser, ftpPassword)) {
                    throw new IOException("could not login to ftp server!!!");
                }
                ftpClient.enterLocalPassiveMode();
                if (!ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) {
                    throw new IOException("Ftp Client could not set filetype(binary file type)!!!");
                }
                if (ftpClient.makeDirectory("/" + productCode)) {
                    ftpClient.changeWorkingDirectory(ftpPath + "/" + productCode);
                    final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                    System.out.println("File");
                    final boolean success = ftpClient.storeFile(fileName, in);
                    ftpClient.logout();
                    if (success) {
                        LOG.debug("File {" + fileName + "} Upload successfully %s");
                    }
                } else {
                    LOG.error("Cannot create  product folder for {" + productCode + "}");
                }
            } catch (Exception e) {
                LOG.error("Error while uploading {" + file.getName() + "}", e);
                eventBus.post(new FtpFailEvent(file));
            } finally {
                ftpClient.disconnect();
            }
        } else {
            LOG.info("SFTP strategy selected");
            Session session = null;
            Channel channel = null;
            ChannelSftp channelSftp = null;
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                String fileName = file.getName();
                JSch jSch = new JSch();
                session = jSch.getSession(ftpUser, ftpHost, ftpPort);
                session.setPassword(ftpPassword);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                channel = session.openChannel(CHANNEL_TYPE);
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                channelSftp.cd(ftpPath);
                //createDirectory(channelSftp, productCode);
                //channelSftp.cd(productCode);
                channelSftp.put(in, fileName);
                LOG.debug("File: {" + file.getName() + "} has been uploaded to ftp server");
            } catch (Exception ex) {
                LOG.error("Failed to upload file: {" + file.getName() + "} to ftp server", ex);
                eventBus.post(new FtpFailEvent(file));
            } finally {
                if (Objects.nonNull(channelSftp)) channelSftp.exit();
                if (Objects.nonNull(channel)) channel.disconnect();
                if (Objects.nonNull(session)) session.disconnect();
            }
        }
    }


    private boolean createDirectory(ChannelSftp channelSftp, String sftpPath) {
        try {
            channelSftp.mkdir(sftpPath);
        } catch (SftpException e) {
            LOG.debug("Failed to create directory", e);
            return false;
        }
        return true;
    }

}
