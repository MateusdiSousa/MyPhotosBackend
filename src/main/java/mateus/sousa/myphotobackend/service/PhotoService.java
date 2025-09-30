package mateus.sousa.myphotobackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import mateus.sousa.myphotobackend.models.PagePhotoResponse;
import mateus.sousa.myphotobackend.models.Photo;
import mateus.sousa.myphotobackend.repository.PhotoRepository;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository repository;

    @Autowired
    private StoreService storageService;

    public Photo savePhoto(MultipartFile file) {
        String filePath = storageService.storeFile(file);
        Photo photo = new Photo(file, filePath);
        return repository.save(photo);
    }

    public Resource getPhotoFile(Long id) throws Exception {
        Photo photo = repository.findById(id).orElseThrow(() -> new Error("Photo not found"));
        return storageService.loadFileResource(photo.getFilePath());
    }

    public Photo getPhoto(Long id) throws Exception {
        Photo photo = repository.findById(id).orElseThrow(() -> new Error("Photo not found"));
        return photo;
    }

    public PagePhotoResponse getPhotos(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<Photo> photoPage = repository.findAll(pageable);

        return new PagePhotoResponse(
                photoPage.getContent(),
                pageSize,
                pageNumber,
                photoPage.hasNext(),
                photoPage.hasPrevious(),
                photoPage.isFirst(),
                photoPage.isLast(),
                photoPage.getTotalElements(),
                photoPage.getTotalPages());
    }

    public void deletePhoto(Long id) throws Exception {
        Photo photo = repository.findById(id)
                .orElseThrow(() -> new Exception("Photo not found with id: " + id));

        storageService.deleteFile(photo.getFilePath());
        repository.delete(photo);
    }

}