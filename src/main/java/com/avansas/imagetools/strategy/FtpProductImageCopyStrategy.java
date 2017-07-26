package com.avansas.imagetools.strategy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class FtpProductImageCopyStrategy implements ProductImageCopyStrategy{
	private static final Logger LOG = LoggerFactory.getLogger(FtpProductImageCopyStrategy.class);
	private static final String CHANNEL_TYPE = "sftp";
	
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
	public void copy(File file, String productCode) {
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
            createDirectory(channelSftp, productCode);
            channelSftp.cd(productCode);
            channelSftp.put(in, fileName);
            LOG.debug("File: {"+ file.getName() +"} has been uploaded to ftp server");
        } catch (Exception ex) {
            LOG.error("Failed to upload file: {"+ file.getName() +"} to ftp server", ex);
        } finally {
            if (Objects.nonNull(channelSftp)) channelSftp.exit();
            if (Objects.nonNull(channel)) channel.disconnect();
            if (Objects.nonNull(session)) session.disconnect();
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
