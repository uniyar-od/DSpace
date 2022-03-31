/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.storage.bitstore.cache;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;

public class S3DownloadManager {

	/** transfer manager */
	private TransferManager transferManager;

	private String bucketName;

	private File tempFile;

	private String key;
	
	private Download download;

	public S3DownloadManager(TransferManager transferManager, String bucketName, String key, File tempFile) {
		super();
		this.transferManager = transferManager;
		this.bucketName = bucketName;
		this.tempFile = tempFile;
		this.key = key;
	}

	public TransferManager getTransferManager() {
		return transferManager;
	}

	public String getBucketName() {
		return bucketName;
	}

	public File getTempFile() {
		return tempFile;
	}

	public String getKey() {
		return key;
	}

	public Download downloadSyncRequest() throws AmazonServiceException, AmazonClientException, InterruptedException {
		Download download = createDownloadObject();
		download.waitForCompletion();
		this.download=download;
		return download;
	}

	public Download downloadAsyncRequest() {
		Download download = createDownloadObject();
		this.download=download;
		return download;
	}

	public Download getDownload() {
		return download;
	}

	private Download createDownloadObject() {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
		Download download = transferManager.download(getObjectRequest, tempFile);
		return download;
	}
}
