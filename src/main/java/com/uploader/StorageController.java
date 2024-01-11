package com.uploader;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/files")
public class StorageController {

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(getConnectionString())
                .buildClient();

        return ResponseEntity.ok(blobServiceClient.getBlobContainerClient("lognathcontainer")
                .listBlobs()
                .stream()
                .map(BlobItem::getName)
                .toList());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(getConnectionString())
                    .buildClient();

            blobServiceClient.getBlobContainerClient("lognathcontainer")
                    .getBlobClient(file.getOriginalFilename())
                    .upload(file.getInputStream(), file.getSize());

            return ResponseEntity.ok("File uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file");
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> handleFileDownload(@PathVariable String filename) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
		        .connectionString(getConnectionString())
		        .buildClient();

		InputStream blobData = blobServiceClient.getBlobContainerClient("lognathcontainer")
		        .getBlobClient(filename)
		        .openInputStream();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", filename);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		return ResponseEntity
		        .ok()
		        .headers(headers)
		        .body(new InputStreamResource(blobData));
    }

    private String getConnectionString() {
        return String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", "lognathstorageaccount", "nm3JAsXt5jP5xxYhVtjUrVeAI22vUaUMPYSiDTlgGWZ2VwuIV0qZb6r1Tc/7/dMWgjo6ed7N1GmT+ASt2z4glw==");
    }
}
