# Documentação do Backend MyPhotos
## Visão Geral
O MyPhotos Backend é uma aplicação Spring Boot que fornece API REST e WebSocket para gerenciamento de fotos e vídeos. Oferece funcionalidades de upload, armazenamento, recuperação e exclusão de arquivos de mídia.

## Arquitetura
### Tecnologias Utilizadas
* Spring Boot - Framework principal
* Spring Data JPA - Persistência de dados
* Spring WebSocket - Comunicação em tempo real
* H2 - Banco de dados
* Lombok - Redução de boilerplate code

## Estrutura de Pacotes:
```text
mateus.sousa.myphotobackend/
├── controller/          # Controladores REST e WebSocket
├── service/            # Lógica de negócio
├── repository/         # Camada de dados
├── models/            # Entidades e DTOs
└── config/            # Configurações
```

## Instalação e Documentação
### Manual
Pré-requisitos:
* Maven
* Java 21

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### Docker
Pré-requisitos:
* Docker e Docker Compose
```bash
docker-compose up
```

## API REST Endpoints

| Método |	Endpoint              |	Descrição              |	Parâmetros |
|--------|------------------------|------------------------|------------------------|
| POST   |	/photos/upload        |	Upload de foto	       | file (MultipartFile)   |
| GET    |	/photos/view          |	Lista fotos paginadas  | page_size, page_number |
| GET    |	/photos/view/{id}     |	Visualiza foto inline  | id (Path)              |
| GET    |	/photos/download/{id} |	Download da foto       | id (Path)              |
| DELETE |	/photos/delete/{id}	  | Exclui foto	           | id (Path)              |

## Modelos de Dados

<b>PagePhotoResponse</b>
```java
public class PagePhotoResponse {
    private List<Photo> content;
    private int pageSize;
    private int pageNumber;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
    private long totalElements;
    private int totalPages;
}
```

<b>MessageResponse</b>
```java
public class MessageResponse {
    private String message;
}
```

<b>UploadFileResponse</b>
```java
public class UploadFileResponse {
    private Photo photo;
    private String message;
}
```

<b>ProgressFileResponse</b>
```java
public class ProgressFileResponse {
    private String filename;
    private int currentChunk;
    private int totalChunk;
}
```

## Serviços Principais
### PhotoService

<b>Funcionalidades</b>

1. Salvar Foto
```java
public Photo savePhoto(MultipartFile file)
```
* Armazena arquivo no sistema de arquivos
* Salva metadados no banco de dados
* Retorna entidade Photo criada

2. Recuperar Arquivo
```java
public Resource getPhotoFile(Long id) throws Exception
```
* Busca foto pelo ID
* Retorna Resource do Spring para streaming

3. Buscar Foto
```java
public Photo getPhoto(Long id) throws Exception
```
* Retorna entidade Photo com metadados

4. Listar Fotos Paginadas
```java
public PagePhotoResponse getPhotos(int pageNumber, int pageSize)
```
* Ordenação por data de criação (descendente)
* Paginação configurável
* Retorna informações completas de paginação

5. Excluir Foto
```java
public void deletePhoto(Long id) throws Exception
```
* Remove arquivo do sistema de arquivos
* Exclui registro do banco de dados

## WebSocket Handler
### PhotoHandler

<b>Protocolo de Comunicação</b>
<b>Conexão Estabelecida:</b>

* Define limite de 100MB para mensagens binárias
* Envia mensagem de confirmação de conexão

#### Tipos de Mensagem

<b>Tipo 1 - Upload Simples (Foto)</b>

```text
[4 bytes]  Tipo (1)
[4 bytes]  Tamanho do filename (N)
[N bytes]  Nome do arquivo
[4 bytes]  Tamanho do content-type (M)
[M bytes]  Content-type
[8 bytes]  Tamanho do arquivo
[X bytes]  Conteúdo do arquivo
```

<b>Tipo 2 - Upload em Chunks (Vídeo)</b>

```text
[4 bytes]  Tipo (2)
[36 bytes] UUID da sessão
[4 bytes]  Tamanho do filename (N)
[N bytes]  Nome do arquivo
[4 bytes]  Tamanho do content-type (M)
[M bytes]  Content-type
[8 bytes]  Tamanho total do arquivo
[4 bytes]  Número do chunk atual
[4 bytes]  Total de chunks
[X bytes]  Conteúdo do chunk
```

#### Fluxo de Upload
1 - Para Fotos (Tipo 1):
* Processamento imediato
* Salvamento único
* Confirmação via WebSocket

2 - Para Vídeos (Tipo 2):
* Agrupamento por UUID
* Ordenação de chunks
* Montagem quando todos chunks são recebidos
* Progresso enviado a cada chunk

#### Classes de Suporte WebSocket
FileInfoWebSocket
```java
class FileInfoWebSocket {
    private String fileName;
    private String contentType;
    private Long size;
    private byte[] content;
}
```

VideoChunkInfo
```java
class VideoChunkInfo {
    private String id;           // UUID da sessão
    private String filename;
    private String contentType;
    private Long totalSize;      // Tamanho total do arquivo
    private byte[] content;      // Conteúdo do chunk
    private Integer chunkNumber; // Número do chunk (1-based)
    private Integer totalChunk;  // Total de chunks
}
```

VideoContent
```java
class VideoContent {
    private String filename;
    private String contentType;
    private ByteBuffer content;  // Conteúdo montado
    private Long size;
}
```

## Armazenamento
### StoreService
* Armazenamento físico de arquivos
* Gerenciamento de recursos (Resource loading)
* Exclusão segura de arquivos
* Caminhos de arquivo organizados

### Estratégia de Armazenamento
* Arquivos salvos em sistema de arquivos
* Metadados em banco de dados relacional
* Suporte a grandes arquivos via chunking

## Segurança e Validações
### Validações Implementadas
* Tamanho máximo de arquivo (100MB por mensagem)
* Integridade de dados via checksums implícitos
* Ordenação de chunks para vídeos grandes
* Tratamento de exceções em todos os níveis
* Tratamento de Erros

```java
try {
    // Processamento WebSocket
} catch (IOException e) {
    logger.warn("Arquivo corrompido: " + e.getMessage());
} catch (RuntimeException e) {
    logger.error("Erro de processamento: " + e.getMessage());
}
```

## Performance
### Otimizações
* Paginação eficiente via Spring Data
* Streaming de arquivos sem carregamento em memória
* Buffer management para grandes arquivos
* Limites configuráveis de tamanho

### WebSocket
* Binary messages para eficiência
* Chunking automático para arquivos grandes
* Progress tracking em tempo real
* Conexões persistentes para múltiplos uploads

## Configuração
### Propriedades Principais
```properties
# Tamanho máximo de upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# WebSocket message size
spring.websocket.binary-message-size-limit=100000000
```

### Configuração WebSocket
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(photoHandler(), "/upload").setAllowedOrigins("*");
    }
}
```
___

<b>Repositório Frontend:</b> <a href="https://github.com/MateusdiSousa/MyPhotosFrontend">MyPhotos Frontend</a>
