package mateus.sousa.myphotobackend.models;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @Column()
    private String originalFilename;

    @Column()
    private String filePath;

    @Column
    private String contentType;

    @Column
    private Long size;

    public Photo(MultipartFile file, String fileName) {
        this.createdAt = LocalDateTime.now();
        this.originalFilename = file.getOriginalFilename();
        this.filePath = fileName;
        this.contentType = file.getContentType();
        this.size = file.getSize();
    }

    public Photo(String originalFilename, String filepath, String contentType, Long size) {
        this.createdAt = LocalDateTime.now();
        this.originalFilename = originalFilename;
        this.filePath = filepath;
        this.contentType = contentType;
        this.size = size;
    }
}
