package com.avansas.imagetools.strategy;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.avansas.imagetools.dto.ConversionGroupListWsDTO;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class FtpImageRenameStrategy extends DefaultImageRenameStrategy implements ImageRenameStrategy{
	private static final String CHANNEL_TYPE = "sftp";
	private static final Logger LOG = LoggerFactory.getLogger(FtpImageRenameStrategy.class);
	
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
	public void renameAll(String productCode, String namePattern) {
		Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try {
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
            //channelSftp.cd(productCode);
            ConversionGroupListWsDTO imageConversionInfo = productImageInfoClient.getImageConversionInfo();
    		String matchString = createMatchString(productCode, imageConversionInfo);
    		String replacement = createReplacementText(namePattern);
    		List<LsEntry> filesToRename = getFilesToRename(channelSftp, productCode, matchString);
    		for(LsEntry file : filesToRename) {
    			renameFile(file, channelSftp, matchString, replacement);
    		}
        } catch (Exception ex) {
            LOG.error("Failed to rename ftp files for product code: {" + productCode + "}", ex);
        } finally {
            if (Objects.nonNull(channelSftp)) channelSftp.exit();
            if (Objects.nonNull(channel)) channel.disconnect();
            if (Objects.nonNull(session)) session.disconnect();
        }
	}


	private void renameFile(LsEntry file, ChannelSftp channelSftp, String matchString, String replacement) {
		String fileName = file.getFilename();
		try {
			String newFileName = fileName.replaceAll(matchString, replacement);
			channelSftp.rename(fileName, newFileName);
			LOG.info("Ftp file: {" + fileName + "} renamed to :{" + newFileName + "}");
		} catch (Exception e) {
			LOG.error("Failed to rename ftp file: {" + fileName + "}", e);
		}
	}


	private List<LsEntry> getFilesToRename(ChannelSftp channelSftp, String productCode, String matchString)
			throws SftpException {
		@SuppressWarnings("unchecked")
		Vector<LsEntry> files = channelSftp.ls(".");
		List<LsEntry> filesToRename = files.parallelStream()
				.filter(file -> file.getFilename().matches(matchString)).collect(Collectors.toList());
		return filesToRename;
	}

}
