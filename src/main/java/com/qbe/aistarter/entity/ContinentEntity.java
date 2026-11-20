package com.qbe.aistarter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "continent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContinentEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
