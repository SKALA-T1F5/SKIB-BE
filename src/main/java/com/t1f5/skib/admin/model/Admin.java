package com.t1f5.skib.admin.model;

import org.hibernate.annotations.Where;

import com.t1f5.skib.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("deprecation") // Hibernate 6.3 이후 @Where 경고 무시
@Where(clause = "is_deleted = false")
@Table(name = "ADMIN")
public class Admin extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "id", length = 40, nullable = false, unique = true)
    private String id;

    @Column(name = "password", length = 120, nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

}
