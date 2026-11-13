package com.qbe.aistarter.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "travel_plan")
public class TravelPlanEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String conversationId;

    @Column(nullable = false)
    private String destination;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @Column(nullable = false)
    private Instant creationDate;
}
