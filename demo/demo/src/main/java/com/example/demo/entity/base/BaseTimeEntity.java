package com.example.demo.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // 우아하게 자동으로 시간을 주입해주는 리스너
public abstract class BaseTimeEntity {

    @CreatedDate //엔티티가 생성되어 저장될때 시간이 자동으로 저장
    @Column(updatable=false) //생성시간은 수정되면 안되므로
    private LocalDateTime createdDate;

    @LastModifiedDate //엔티티값이 변경될때 시간이 자동으로 변경
    private LocalDateTime modifiedDate;

    private LocalDateTime deletedDate;

    public void setDeleteDate() {
        this.deletedDate = LocalDateTime.now();
    }
}
