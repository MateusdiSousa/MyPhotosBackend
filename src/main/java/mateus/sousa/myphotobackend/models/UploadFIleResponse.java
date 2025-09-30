package mateus.sousa.myphotobackend.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UploadFIleResponse {
    private Photo photo;
    private String message;
}
