package mateus.sousa.myphotobackend.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


@Service
public class StoreService {
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            Path targetLocation = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new Error("Could not store file: " + file.getOriginalFilename(), ex);
        }
    }

    public String storeFile(String originalFilename, byte[] content) {
        try {
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path targetPath = Paths.get(uploadDir).resolve(filename);
            Files.createFile(targetPath);
            Files.write(targetPath, content);
            return filename;
        } catch (Exception e) {
            throw new Error("Fail to write the file: "+ e);
        }
    }

    public Resource loadFileResource(String fileName) throws Exception {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new Error("File not found " + fileName);
            }

        } catch (MalformedURLException e) {
            throw new Error("File not found " + e.getMessage());
        }
    }

    public void deleteFile(String fileName) throws Exception {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new Exception("Could not delete " + fileName, e);
        }
    }
}
