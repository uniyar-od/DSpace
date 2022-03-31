/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.storage.bitstore.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.storage.bitstore.DeleteOnCloseFileInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;

public class S3CachingSystem {

	/** transfer manager */
	private TransferManager transferManager;

	private String bucketName;

	private Map<String, File> cachedFiles = Collections.synchronizedMap(new HashMap<String, File>());
	private Map<String, S3DownloadManager> downloadingFiles = Collections
			.synchronizedMap(new HashMap<String, S3DownloadManager>());

	/** log4j log */
	private static Logger log = Logger.getLogger(S3CachingSystem.class);

	public S3CachingSystem(TransferManager transferManager, String bucketName) {
		super();
		this.transferManager = transferManager;
		this.bucketName = bucketName;
	}

	public InputStream getInputStreamForBitstream(String key, boolean isCacheEnabled) throws IOException {
		if (isCacheEnabled) {
			return cacheEnabledFlow(key);
		} else {
			return cacheDisabledFlow(key);
		}
	}

	private InputStream cacheDisabledFlow(String key) throws IOException {
		try {
			File tempFile = File.createTempFile("s3-disk-copy", "temp");
			tempFile.deleteOnExit();

			S3DownloadManager downloadManager = new S3DownloadManager(transferManager, bucketName, key, tempFile);
			downloadManager.downloadSyncRequest();

			return new DeleteOnCloseFileInputStream(tempFile);
		} catch (Exception e) {
			log.error("get(" + key + ")", e);
			throw new IOException(e);
		}
	}

	private InputStream cacheEnabledFlow(String key) throws IOException {
		try {
			return retrieveInputStream(key);
		} catch (Exception e) {
			log.error("get(" + key + ")", e);
			throw new IOException(e);
		}
	}

	private InputStream retrieveInputStream(String key)
			throws IOException, AmazonServiceException, AmazonClientException, InterruptedException {
		if (isFileIsAvailableInCache(key)) {
			return new FileInputStream(cachedFiles.get(key));
		} else {
			return downloadFile(key);
		}
	}

	private InputStream downloadFile(String key)
			throws IOException, AmazonServiceException, AmazonClientException, InterruptedException {
		S3DownloadManager s3DownloadManager;
		Download download;
		// need to add the file to the cache and handle the download process
		// perform another check to see if another (just ended) execution of this
		// synchronized block has managed to retrieve the file
		if (isFileIsAvailableInCache(key)) {
			return new FileInputStream(cachedFiles.get(key));
		} else {
			// to be sure when the file is deleted we're
			// no longer referencing it
			cachedFiles.remove(key);
		}
		// if the file is not available in cache
		// then check if is downloading and wait for its completion
		s3DownloadManager = downloadingFiles.get(key);
		if (s3DownloadManager != null) {
			download = s3DownloadManager.getDownload();
			download.waitForCompletion();
			downloadingFiles.remove(key);
			return new FileInputStream(s3DownloadManager.getTempFile());
		}
		File tempFile;
		String tmpDir = ConfigurationManager.getProperty("assetstore.s3.local.cache.dir");
		if (StringUtils.isNotBlank(tmpDir)) {
			tempFile = File.createTempFile("s3-disk-copy", "temp", new File(tmpDir));
		} else {
			tempFile = File.createTempFile("s3-disk-copy", "temp");
		}

		s3DownloadManager = new S3DownloadManager(transferManager, bucketName, key, tempFile);
		// start download and save the downloading file reference
		download = s3DownloadManager.downloadAsyncRequest();
		downloadingFiles.put(key, s3DownloadManager);
		// wait for the download to complete and add the file in cached files
		// also remove the file from the downloading list
		download.waitForCompletion();
		cachedFiles.put(key, tempFile);
		downloadingFiles.remove(key);

		return new FileInputStream(tempFile);

	}

	private boolean isFileIsAvailableInCache(String key) {
		return cachedFiles.containsKey(key) && cachedFiles.get(key).exists();
	}
}
