package com.megansoft.training.gcplabs.controller;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
@RequestMapping("/gcs")
public class GCSOperationsController {
	
	
	public void downloadObject(String projectId, String bucketName, String objectName, String destFilePath) {
		Storage storage = getStorage(projectId, destFilePath);

		Blob blob = storage.get(BlobId.of(bucketName, objectName));
		if (blob != null) {
			blob.downloadTo(Paths.get(destFilePath));

			System.out.println("The object gs://" + bucketName + "/" + objectName + " is downloaded to " + destFilePath);
		} else {
			System.out.println("The object gs://" + bucketName + "/" + objectName + "  is not available ");

		}
	}

	private Storage getStorage(String projectId, String destFilePath) {
		if (destFilePath != null) {
			File parentFile = new File(destFilePath).getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		}
		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		return storage;
	}

	/*
	 * public void downloadObjectByStream(String projectId, String bucketName,
	 * String objectName, String destFilePath) { Storage storage =
	 * getStorage(projectId, destFilePath);
	 * 
	 * BlobId blobId = BlobId.of(bucketName, objectName); try (ReadChannel reader =
	 * storage.reader(blobId)) { ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
	 * try { while (reader.read(bytes) > 0) { bytes.flip(); // do something with
	 * bytes bytes.clear(); } } catch (IOException e) { e.printStackTrace(); } } }
	 */

	public void uploadObject(String projectId, String bucketName, String objectName, String content) {

		try {
			Storage storage = getStorage(projectId, null);
			BlobId blobId = BlobId.of(bucketName, objectName);
			byte[] contentBytes = content.getBytes();
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
			try (WriteChannel writer = storage.writer(blobInfo)) {
				writer.write(ByteBuffer.wrap(contentBytes, 0, contentBytes.length));
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@GetMapping("/{projectId}/{bucketName}/list")
	public List<String> getObjects(@PathVariable("projectId") String projectId, @PathVariable("bucketName") String bucketName) {
		Storage storage = getStorage(projectId, null);
		
	    Page<Blob> blobs = storage.list(bucketName);
	    List<String> objects = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
        	objects.add(blob.getName());
        }
        
        return objects;
	}
	
	@PostMapping("/{projectId}/{bucketName}/list")
	public List<String> getObjectsWithPrefix(@PathVariable("projectId") String projectId, @PathVariable("bucketName") String bucketName, 
			@RequestBody String directoryPrefix) {
		Storage storage = getStorage(projectId, null);
		
	    Page<Blob> blobs = null;
	    
	    if(StringUtils.isNotBlank(directoryPrefix)) {
	    	blobs = storage.list(
	                bucketName,
	                Storage.BlobListOption.prefix(directoryPrefix),
	                Storage.BlobListOption.currentDirectory());
	    } else {
	    	blobs = storage.list(bucketName);
	    }
	    List<String> objects = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
        	objects.add(blob.getName());
        }
        
        return objects;
	}

	@PutMapping("/{projectId}/{bucketName}/remove")
	public void removeObject(@PathVariable("projectId") String projectId, @PathVariable("bucketName") String bucketName, 
			@RequestBody String objectName) {
		Storage storage = getStorage(projectId, null);
		BlobId blobId = BlobId.of(bucketName, objectName);
		if (storage.delete(blobId)) {
			System.out.println("gs://" + bucketName + "/" + objectName + " is removed");
		} else {
			System.out.println("gs://" + bucketName + "/" + objectName + " unable to remove");
		}
	}

	@GetMapping("/{projectId}/buckets")
	public List<String> listBuckets(@PathVariable("projectId") String projectId) {
		Storage storage = this.getStorage(projectId, null);
		Page<Bucket> bucketsItems = storage.list();
		List<String> bukcets = new ArrayList<>();
		for (Bucket bucket : bucketsItems.iterateAll()) {
			bukcets.add("gs://" + bucket.getName() + "/");
		}
		return bukcets;
	}
}
