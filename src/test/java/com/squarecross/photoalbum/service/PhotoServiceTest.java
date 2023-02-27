package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PhotoServiceTest {

    @Autowired
    PhotoRepository photoRepository;

    @Autowired
    AlbumRepository albumRepository;

    @Test
    void getPhoto() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        Photo photo = new Photo();
        photo.setFileName("테스트 파일");
        photo.setAlbum(savedAlbum);
        Photo savedPhoto = photoRepository.save(photo);

        Optional<Photo> photoOptional = photoRepository.findById(savedPhoto.getPhotoId());
        assertNotNull(photoOptional.get());
    }
}