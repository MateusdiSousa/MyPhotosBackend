package mateus.sousa.myphotobackend.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagePhotoResponse {
    private List<Photo> photos;
    private int pageSize;
    private int pageNumber;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private boolean isFirstPage;
    private boolean isLastPage;
    private long totalPhotos;
    private int totalPages;
}
