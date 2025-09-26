package com.company.wolbu.assignment.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 도메인 엔티티
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    public static Member create(String name, String email, String phone, String passwordHash, MemberRole role) {
        Member member = new Member();
        member.name = name;
        member.email = email;
        member.phone = phone;
        member.passwordHash = passwordHash;
        member.role = role;
        return member;
    }

    public boolean isInstructor() {
        return this.role == MemberRole.INSTRUCTOR;
    }

    public boolean isStudent() {
        return this.role == MemberRole.STUDENT;
    }
}


