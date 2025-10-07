package mateus.sousa.myphotobackend.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mateus.sousa.myphotobackend.models.Photo;
import mateus.sousa.myphotobackend.models.ProgressFileResponse;
import mateus.sousa.myphotobackend.models.UploadFileResponse;
import mateus.sousa.myphotobackend.repository.PhotoRepository;
import mateus.sousa.myphotobackend.service.StoreService;

public class PhotoHandler extends AbstractWebSocketHandler {
    @Autowired
    private StoreService storeService;

    @Autowired
    private PhotoRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(PhotoHandler.class);

    private final Map<String, ArrayList<VideoChunkInfo>> videoChunks = new HashMap<String, ArrayList<VideoChunkInfo>>();

    private final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Connection established!");
        session.sendMessage(new TextMessage("{\"status\":\"connected\",\"message\":\"Conexão estabelecida\"}"));
        session.setBinaryMessageSizeLimit(100 * 1024 * 1024); // 10 MB de limite
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Connection closed. Status: " + status.getReason());
    }

    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        try {

            ByteBuffer payload = message.getPayload();

            Integer saveType = payload.getInt();

            switch (saveType.intValue()) {
                case 1:
                    FileInfoWebSocket fileInfo = deserializePhoto(payload);
                    logger.info("Photo \"" + fileInfo.getFileName() + "\" desserialized");
                    this.saveFile(fileInfo, session);
                    logger.info("Photo " + fileInfo.getFileName() + " saved successfully");
                    break;
                case 2:
                    VideoChunkInfo chunk = deserializeVideo(payload);
                    String uuid = chunk.getId();

                    if (!videoChunks.containsKey(uuid)) {
                        ArrayList<VideoChunkInfo> newVideoChunk = new ArrayList<VideoChunkInfo>(chunk.getTotalChunk());
                        videoChunks.put(chunk.getId(), newVideoChunk);
                        logger.info("Receiving a new video");
                    }

                    ArrayList<VideoChunkInfo> video = videoChunks.get(uuid);
                    video.add(chunk);

                    logger.info(String.format("Video \"%s\" %s/%s completed", chunk.getFilename(), video.size(),
                            chunk.getTotalChunk()));

                    if (chunk.getTotalChunk().intValue() == video.size()) {
                        VideoContent videoContent = mountVideo(chunk.getId());
                        this.saveVideo(videoContent, session);
                        videoChunks.remove(uuid);
                        logger.info("Video \"" + videoContent.getFilename() + "\" saved successfully");
                        break;
                    }

                    ProgressFileResponse progressResponse = new ProgressFileResponse(chunk.getFilename(), video.size(),
                            chunk.getTotalChunk());

                    session.sendMessage(new TextMessage(mapper.writeValueAsString(progressResponse)));
                    break;
                default:
                    break;
            }

        } catch (IOException e) {
            logger.warn(e.getMessage());
            session.sendMessage(new TextMessage("Arquivo enviado está corrompido"));
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            session.sendMessage(new TextMessage("Ocorreu um erro ao descomprimir o arquivo"));
        }
    }

    private VideoChunkInfo deserializeVideo(ByteBuffer buffer) {
        return getVideoChunkInfo(buffer);
    }

    private FileInfoWebSocket deserializePhoto(ByteBuffer buffer) {
        FileInfoWebSocket fileInfo = getFileInfoFromBuffer(buffer);

        fileInfo.setContent(new byte[buffer.remaining()]);

        buffer.get(fileInfo.getContent());

        return fileInfo;
    }

    private FileInfoWebSocket getFileInfoFromBuffer(ByteBuffer buffer) {
        FileInfoWebSocket fileInfo = new FileInfoWebSocket();

        int filenameSize = buffer.getInt();
        fileInfo.setFileName(readStringBuffer(filenameSize, buffer));

        int contentTypeSize = buffer.getInt();
        fileInfo.setContentType(readStringBuffer(contentTypeSize, buffer));

        Long size = buffer.getLong();
        fileInfo.setSize(size);

        return fileInfo;
    }

    private VideoChunkInfo getVideoChunkInfo(ByteBuffer buffer) {
        String uuid = readStringBuffer(36, buffer);

        Integer filenameSize = buffer.getInt();
        String filename = readStringBuffer(filenameSize, buffer);

        Integer contentyTypeSize = buffer.getInt();
        String contentType = readStringBuffer(contentyTypeSize, buffer);

        Long totalSize = buffer.getLong();

        int chunkNumber = buffer.getInt();
        int totalChunk = buffer.getInt();

        byte[] chunkContent = new byte[buffer.remaining()];
        buffer.get(chunkContent);

        return new VideoChunkInfo(uuid, filename, contentType, totalSize, chunkContent, chunkNumber,
                totalChunk);
    }

    private String readStringBuffer(int size, ByteBuffer buffer) {
        byte[] data = new byte[size];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    private UploadFileResponse saveFile(FileInfoWebSocket fileInfo, WebSocketSession session) throws IOException {
        String filepath = this.storeService.storeFile(fileInfo.getFileName(), fileInfo.getContent());
        Photo photo = new Photo(fileInfo.getFileName(), filepath, fileInfo.getContentType(), fileInfo.getSize());
        repository.save(photo);

        UploadFileResponse uploadFIle = new UploadFileResponse();

        uploadFIle.setPhoto(photo);
        uploadFIle.setMessage(fileInfo.getFileName() + " salvo com sucesso");

        session.sendMessage(new TextMessage(mapper.writeValueAsString(uploadFIle)));

        return uploadFIle;
    }

    private UploadFileResponse saveVideo(VideoContent videoContent, WebSocketSession session) throws IOException {
        String filepath = this.storeService.storeFile(videoContent.getFilename(), videoContent.getContent().array());
        Photo photo = new Photo(videoContent.getFilename(), filepath, videoContent.getContentType(),
                videoContent.getSize());
        repository.save(photo);

        UploadFileResponse uploadFIleResponse = new UploadFileResponse();
        uploadFIleResponse.setPhoto(photo);
        uploadFIleResponse.setMessage(videoContent.getFilename() + " salvo com sucesso");

        session.sendMessage(new TextMessage(mapper.writeValueAsString(uploadFIleResponse)));

        return uploadFIleResponse;
    }

    private VideoContent mountVideo(String id) {
        List<VideoChunkInfo> chunks = this.videoChunks.get(id);

        VideoChunkInfo firstChunk = chunks.getFirst();

        VideoContent videoContent = new VideoContent();

        Long videoTotalSize = (long) 0;

        for (VideoChunkInfo videoChunkInfo : chunks) {
            videoTotalSize += videoChunkInfo.getContent().length;
        }

        videoContent.setContent(ByteBuffer.allocate(videoTotalSize.intValue()));
        videoContent.setFilename(firstChunk.getFilename());
        videoContent.setContentType(firstChunk.getContentType());
        videoContent.setSize(firstChunk.getTotalSize());

        List<VideoChunkInfo> result = chunks.stream()
                .sorted((chunkA, chunkB) -> chunkA.getChunkNumber().compareTo(chunkB.getChunkNumber()))
                .collect(Collectors.toList());

        for (VideoChunkInfo videoChunkInfo : result) {
            videoContent.getContent().put(videoChunkInfo.getContent());
        }

        logger.info("Video monted successfully");

        return videoContent;
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class FileInfoWebSocket {
    private String fileName;
    private String contentType;
    private Long size;
    private byte[] content;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class VideoChunkInfo {
    private String id;
    private String filename;
    private String contentType;
    private Long totalSize;
    private byte[] content;
    private Integer chunkNumber;
    private Integer totalChunk;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class VideoContent {
    private String filename;
    private String contentType;
    private ByteBuffer content;
    private Long size;
}