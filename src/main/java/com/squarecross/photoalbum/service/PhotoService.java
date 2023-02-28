package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    private final String original_path = Constants.PATH_PREFIX + "/photos/original";
    private final String thumb_path = Constants.PATH_PREFIX + "/photos/thumb";
    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private AlbumRepository albumRepository;

    public PhotoDto getPhoto(Long PhotoId) {
        Optional<Photo> photo = photoRepository.findById(PhotoId);
        if(photo.isPresent()) {
            PhotoDto photoDto = PhotoMapper.convertToDto(photo.get());
            return photoDto;
        } else {
            throw new EntityNotFoundException("Photo ID " + PhotoId + "가 존재하지 않습니다.");
        }
    }

    public List<PhotoDto> getPhotoList(String keyword, String sort, String orderBy) {
        List<Photo> photos;
        if (Objects.equals(sort, "byDate")) {
            if (Objects.equals(orderBy, "asc")) {
                photos = photoRepository.findByFileNameContainingOrderByUploadedAtDesc(keyword);
            } else {
                photos = photoRepository.findByFileNameContainingOrderByUploadedAtAsc(keyword);
            }
        } else if (Objects.equals(sort, "byName")){
            if (Objects.equals(orderBy, "asc")) {
                photos = photoRepository.findByFileNameContainingOrderByFileNameAsc(keyword);
            } else {
                photos = photoRepository.findByFileNameContainingOrderByFileNameDesc(keyword);
            }
        } else {
            throw new EntityNotFoundException("알 수 없는 정렬 기준입니다.");
        }

        List<PhotoDto> photoDtos = PhotoMapper.convertToDtoList(photos);
        return photoDtos;
    }

    public List<PhotoDto> movePhotos(
            Long fromAlbumId, Long toAlbumId, List<Long> photoIds) throws IOException {
        List<Photo> photos = new ArrayList<>();
        for (Long photoId : photoIds) {
            Optional<Photo> res = photoRepository.findById(photoId);
            if (res.isPresent()) {
                Photo photo = res.get();
                File originalFile = new File(Constants.PATH_PREFIX + photo.getOriginalUrl());
                File thumbFile = new File(Constants.PATH_PREFIX + photo.getThumbUrl());

                String fileName = photo.getFileName();
                fileName = getNextFileName(fileName, toAlbumId);
                photo.setOriginalUrl("/photos/original/" + toAlbumId + "/" + fileName);
                photo.setThumbUrl("/photos/thumb/" + toAlbumId + "/" + fileName);

                File originalChangeFile = new File(Constants.PATH_PREFIX + photo.getOriginalUrl());
                File thumbChangeFile = new File(Constants.PATH_PREFIX + photo.getThumbUrl());

                Files.copy(originalFile.toPath(), originalChangeFile.toPath());
                Files.copy(thumbFile.toPath(), thumbChangeFile.toPath());

                deleteFiles(String.valueOf(originalFile.toPath()));
                deleteFiles(String.valueOf(thumbFile.toPath()));

                photos.add(photo);
            } else {
                throw new EntityNotFoundException("에러");
            }
        }
        return PhotoMapper.convertToDtoList(photos);
    }

    private void deleteFiles(String url) throws IOException {
        Files.delete(Path.of(url));
    }

    public List<PhotoDto> deletePhotos(List<Long> photoIds) throws IOException {
        List<Photo> photos = new ArrayList<>();
        for (Long photoId : photoIds) {
            Optional<Photo> res = photoRepository.findById(photoId);
            if (res.isPresent()) {
                Photo photo = res.get(); photos.add(photo);
                deleteFiles(Constants.PATH_PREFIX + photo.getOriginalUrl());
                deleteFiles(Constants.PATH_PREFIX + photo.getThumbUrl());
                photoRepository.deleteById(photoId);
            } else {
                throw new EntityNotFoundException("에러");
            }
        }
        return PhotoMapper.convertToDtoList(photos);
    }

    public PhotoDto savePhoto(MultipartFile file, Long albumId){
        Optional<Album> res = albumRepository.findById(albumId);
        if(res.isEmpty()){
            throw new EntityNotFoundException("앨범이 존재하지 않습니다.");
        }

        String fileName = file.getOriginalFilename();
        int fileSize = (int) file.getSize();
        fileName = getNextFileName(fileName, albumId);
        saveFile(file, albumId, fileName);

        Photo photo = new Photo();
        photo.setOriginalUrl("/photos/original/" + albumId + "/" + fileName);
        photo.setThumbUrl("/photos/thumb/" + albumId + "/" + fileName);
        photo.setFileName(fileName);
        photo.setFileSize(fileSize);
        photo.setAlbum(res.get());
        Photo createdPhoto = photoRepository.save(photo);
        return PhotoMapper.convertToDto(createdPhoto);
    }

    public File getImageFile(Long photoId) {
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isEmpty()) {
            throw new EntityNotFoundException("사진을 ID " + photoId + "을 찾을 수 없습니다.");
        }
        return new File(Constants.PATH_PREFIX + res.get().getOriginalUrl());
    }

    private String getNextFileName(String fileName, Long albumId) {
        String fileNameNoExt = StringUtils.stripFilenameExtension(fileName);
        String ext = StringUtils.getFilenameExtension(fileName);

        Optional<Photo> res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);

        int count = 2;
        while(res.isPresent()) {
            fileName = String.format("%s (%d).%s", fileNameNoExt, count, ext);
            res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);
            count++;
        }

        return fileName;
    }

    private void saveFile(MultipartFile file, Long AlbumId, String fileName){
        try {
            String filePath = AlbumId + "/" + fileName;
            Files.copy(file.getInputStream(), Paths.get(original_path + "/" + filePath));

            BufferedImage thumbImg = Scalr.resize(ImageIO.read(file.getInputStream()), Constants.THUMB_SIZE, Constants.THUMB_SIZE);
            File thumbFile = new File(thumb_path + "/" + filePath);
            String ext = StringUtils.getFilenameExtension(fileName);
            if (ext == null) {
                throw new IllegalArgumentException("No Extention");
            }
            ImageIO.write(thumbImg, ext, thumbFile);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public boolean checkFile(MultipartFile file) {
        String mimeType = file.getContentType();
        return mimeType.startsWith("image");
    }
}
