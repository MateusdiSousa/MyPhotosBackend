package mateus.sousa.myphotobackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import mateus.sousa.myphotobackend.models.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long>{
}
