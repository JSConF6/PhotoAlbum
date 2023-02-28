package com.squarecross.photoalbum.mapper;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;

import java.util.List;
import java.util.stream.Collectors;

public class PhotoMapper {
    public static PhotoDto convertToDto(Photo photo){
        PhotoDto photoDto = new PhotoDto();
        photoDto.setPhotoId(photo.getPhotoId());
        photoDto.setAlbumId(photo.getAlbum().getAlbumId());
        photoDto.setFileName(photo.getFileName());
        photoDto.setFileSize(photo.getFileSize());
        photoDto.setOriginalUrl(photo.getOriginalUrl());
        photoDto.setThumbUrl(photo.getThumbUrl());
        photoDto.setUploadedAt(photo.getUploadedAt());
        return photoDto;
    }

    public static List<PhotoDto> convertToDtoList(List<Photo> photos) {
        List<PhotoDto> photoDtos = photos.stream().map(PhotoMapper::convertToDto)
                .collect(Collectors.toList());
        return photoDtos;
    }
}
