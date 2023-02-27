package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 스프링 컴테이너 내에 있는 모든 빈을 DI로 가져와서 사용할 수 있도록 만듭니다.
@Transactional // 테스트로 생성한 데이터는 실제로 DB에 반영되지 않도록 Commit을 하지 않는다.
class AlbumServiceTest {

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    PhotoRepository photoRepository;

    @Autowired
    AlbumService albumService;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        AlbumDto resAlbum = albumService.getAlbum(savedAlbum.getAlbumId());
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

    @Test
    void testPhotoCount() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        // 사진을 생성하고, setAlbum을 통해 앨범을 지정해준 이후, repository을 사진을 저장한다.
        Photo photo1 = new Photo();
        photo1.setFileName("사진1");
        photo1.setAlbum(savedAlbum);
        photoRepository.save(photo1);

        Photo photo2 = new Photo();
        photo2.setFileName("사진1");
        photo2.setAlbum(savedAlbum);
        photoRepository.save(photo2);

        AlbumDto albumDto = albumService.getAlbum(savedAlbum.getAlbumId());
        assertEquals(2, albumDto.getCount());
    }

    @Test
    void testAlbumCreate() throws IOException {
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("나의 앨범");

        AlbumDto album = albumService.createAlbum(albumDto);
        assertNotNull(album);

        Files.delete(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + album.getAlbumId()));
        Files.delete(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + album.getAlbumId()));
    }

    @Test
    void testAlbumRepository() throws InterruptedException{
        Album album1 = new Album();
        Album album2 = new Album();
        album1.setAlbumName("aaaa");
        album2.setAlbumName("aaab");

        albumRepository.save(album1);
        TimeUnit.SECONDS.sleep(1); // 시간차를 벌리기 위해 두번째 앨범 생성 1초 딜레이
        albumRepository.save(album2);

        // 최신순 정렬, 두번째로 생성한 앨범이 먼저 나와야합니다.
        List<Album> resDate = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc("aaa");
        assertEquals("aaab", resDate.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals("aaaa", resDate.get(1).getAlbumName()); // 1번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals(2, resDate.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크

        // 최신순 정렬, 두번째로 생성한 앨범이 먼저 나와야합니다.
        List<Album> resName = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc("aaa");
        assertEquals("aaaa", resDate.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals("aaab", resDate.get(1).getAlbumName()); // 1번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals(2, resDate.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크
    }
}