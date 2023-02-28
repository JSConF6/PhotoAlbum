package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.service.PhotoService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/albums/{albumId}/photos")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    public ResponseEntity<PhotoDto> getPhotoInfo(@PathVariable("photoId") Long photoId) {
        PhotoDto photo = photoService.getPhoto(photoId);
        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<List<PhotoDto>> uploadPhotos(@PathVariable("albumId") final long albumId,
                                                       @RequestParam("photos") MultipartFile[] files){
        List<PhotoDto> photos = new ArrayList<>();
        for (MultipartFile file : files) {
            if(!photoService.checkFile(file)) {
                throw new IllegalArgumentException("이미지 파일이 아닙니다.");
            }
            PhotoDto photoDto = photoService.savePhoto(file, albumId);
            photos.add(photoDto);
        }
        return new ResponseEntity<>(photos, HttpStatus.OK);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadPhotos(@RequestParam("photoIds") Long[] photoIds, HttpServletResponse response) {
        try{
            if(photoIds.length == 1) {
                File file = photoService.getImageFile(photoIds[0]);
                OutputStream outputStream = response.getOutputStream();
                IOUtils.copy(new FileInputStream(file), outputStream);
                outputStream.close();
            } else {
                ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
                for(Long photoId : photoIds) {
                    File file = photoService.getImageFile(photoId);
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    IOUtils.copy(new FileInputStream(file), zipOutputStream);
                }
                zipOutputStream.close();
            }
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Error");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    public ResponseEntity<List<PhotoDto>> getPhotoList(
            @RequestParam(value = "sort", required = false, defaultValue = "byDate") String sort,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "orderBy", required = false, defaultValue = "desc") String orderBy) {
        List<PhotoDto> photoList = photoService.getPhotoList(keyword, sort, orderBy);
        return new ResponseEntity<>(photoList, HttpStatus.OK);
    }

    @PutMapping("/move")
    public ResponseEntity<List<PhotoDto>> movePhotos(
            @RequestParam("fromAlbumId") Long fromAlbumId,
            @RequestParam("toAlbumId") Long toAlbumId,
            @RequestParam("photoIds") List<Long> photoIds) throws IOException {
        List<PhotoDto> photoDtos = photoService.movePhotos(fromAlbumId, toAlbumId, photoIds);
        return new ResponseEntity<>(photoDtos, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<List<PhotoDto>> deletePhotos(
            @RequestBody PhotoDto photoDto) throws IOException {
        List<Long> photoIds = photoDto.getPhotoIds();
        List<PhotoDto> photoDtos = photoService.deletePhotos(photoIds);
        return new ResponseEntity<>(photoDtos, HttpStatus.OK);
    }
}
