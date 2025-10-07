package mateus.sousa.myphotobackend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAsync
public class MyPhotosBackendApplication implements CommandLineRunner {
	@Value("${file.upload-dir}")
	private String uploadDir;

	public static void main(String[] args) {
		SpringApplication.run(MyPhotosBackendApplication.class, args);
	}

	public void run(String... args) throws Exception {
		Path uploadPath = Paths.get(uploadDir);
		
		if (!Files.notExists(uploadPath) || !Files.isDirectory(uploadPath) ) {
			Files.createDirectories(Paths.get(uploadDir));
			System.out.println("Diretório de upload criado: "+ uploadDir);	
		}
	}
}
