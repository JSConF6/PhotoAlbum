package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;

    public PhotoDto getPhoto(Long PhotoId) {
        Optional<Photo> photo = photoRepository.findById(PhotoId);
        if(photo.isPresent()) {
            PhotoDto photoDto = PhotoMapper.convertToDto(photo.get());
            return photoDto;
        } else {
            throw new EntityNotFoundException("Photo ID " + PhotoId + "가 존재하지 않습니다.");
        }
    }
}
