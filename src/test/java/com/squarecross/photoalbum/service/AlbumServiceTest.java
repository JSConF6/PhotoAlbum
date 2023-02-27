package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 스프링 컴테이너 내에 있는 모든 빈을 DI로 가져와서 사용할 수 있도록 만듭니다.
@Transactional // 테스트로 생성한 데이터는 실제로 DB에 반영되지 않도록 Commit을 하지 않는다.
class AlbumServiceTest {

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    AlbumService albumService;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        Album resAlbum = albumService.getAlbum(savedAlbum.getAlbumId());
        assertEquals("테스트", resAlbum.getAlbumName());
    }

    @Test
    void getAlbumException() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        Throwable exception = assertThrows(EntityNotFoundException.class, () -> {
            albumService.getAlbum(Long.valueOf(2));
        }, "예외가 발생하지 않았습니다.");
        assertEquals("앨범 아이디 2로 조회되지 않았습니다.", exception.getMessage());
    }



    @Test
    void getAlbumByAlbumName() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        Album resAlbum = albumService.getAlbumByAlbumName(savedAlbum.getAlbumName());
        assertEquals("테스트", resAlbum.getAlbumName());
    }

    @Test
    void getAlbumByAlbumNameException() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        Throwable exception = assertThrows(EntityNotFoundException.class, () -> {
            albumService.getAlbumByAlbumName("테스트1");
        }, "예외가 발생하지 않았습니다.");
        assertEquals("앨범 이름 테스트1로 조회되지 않았습니다.", exception.getMessage());
    }
}