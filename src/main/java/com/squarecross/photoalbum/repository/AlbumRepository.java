package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.dto.AlbumDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByAlbumName(String albumName);

    List<Album> findByAlbumNameContainingOrderByCreatedAtAsc(String keyword);
    List<Album> findByAlbumNameContainingOrderByCreatedAtDesc(String keyword);
    List<Album> findByAlbumNameContainingOrderByAlbumNameAsc(String keyword);
    List<Album> findByAlbumNameContainingOrderByAlbumNameDesc(String keyword);
}
