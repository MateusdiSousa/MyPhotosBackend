package mateus.sousa.myphotobackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import mateus.sousa.myphotobackend.models.MessageResponse;
import mateus.sousa.myphotobackend.models.PagePhotoResponse;
import mateus.sousa.myphotobackend.models.Photo;
import mateus.sousa.myphotobackend.service.PhotoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("photos")
@Controller
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @PostMapping("/upload")
    public ResponseEntity<String> savePhoto(@RequestParam("file") MultipartFile file) {
        photoService.savePhoto(file);
        return ResponseEntity.ok("Photo saved");
    }

    @GetMapping("/view")
    public ResponseEntity<PagePhotoResponse> getPhotos(@RequestParam("page_size") int pageSize,
            @RequestParam("page_number") int pageNumber) {
        return ResponseEntity.ok(photoService.getPhotos(pageNumber, pageSize));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewPhoto(@PathVariable Long id) throws Exception {
        Resource resource = photoService.getPhotoFile(id);
        Photo photo = photoService.getPhoto(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFilePath() + "\"")
                .body(resource);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long id) throws Exception {
        Resource resource = photoService.getPhotoFile(id);
        Photo photo = photoService.getPhoto(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photo.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/batch")
    public ResponseEntity<Resource> downloadPhotoBatch(@RequestParam List<String> ids) throws Exception {
        List<Long> params = ids.stream().map(id -> Long.parseLong(id)).toList();
        
        Resource resource = photoService.downloadBatchPhoto(params);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressedPhotos.zip\"")
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageResponse> deletePhotos(@PathVariable Long id) throws Exception {
        photoService.deletePhoto(id);
        return ResponseEntity.ok().body(new MessageResponse("Photo deleted"));
    }

}
