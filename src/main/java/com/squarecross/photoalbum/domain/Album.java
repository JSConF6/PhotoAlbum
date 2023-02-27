package com.squarecross.photoalbum.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="album", schema = "photo_album", uniqueConstraints = {@UniqueConstraint(columnNames = "album_id")}) // name: 테이블명, schema: 스키마명, uniqueConstraints: 반복되면 안되는 제약조건
@EntityListeners(AuditingEntityListener.class)
public class Album {

    @Id // 해당 Entity의 Primary Key로 사용한다는 의미
    @GeneratedValue(strategy = GenerationType.IDENTITY) // @Id 값을 새롭게 부여할 때 사용하는 방법에 대한 정보를 입력 strategy = GenerationType.IDENTITY 가장 최근 id에 +1을 해서 다음 아이디를 생성
    @Column(name = "album_id", unique = true, nullable = false) // album 테이블의 매핑되는 column 정보를 입력합니다. name: Column명, unique: 중복불가, nullable: null값 허용
    private Long albumId;

    @Column(name = "album_name", unique = false, nullable = false)
    private String albumName;

    @Column(name = "created_at", unique = false, nullable = true)
    @CreatedDate // 새로운 앨범을 생성해 DB INSERT할 때 자동으로 현재 시간을 입력해줍니다.
    private Date createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "album", cascade = CascadeType.ALL)
    private List<Photo> photos;

    public Album() {
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
