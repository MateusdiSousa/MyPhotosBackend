package mateus.sousa.myphotobackend.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgressFileResponse {
    private String filename;
    private Integer chunksSended;
    private Integer totalChunks;
}
