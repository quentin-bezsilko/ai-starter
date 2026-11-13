package com.qbe.aistarter.loader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RagLoader {

    /* private final VectorStore vectorStore;

    @PostConstruct
    void init() {

        vectorStore.add(
                List.of(
                        new Document(
                                """
                        Paris est célèbre pour la Tour Eiffel,
                        le Louvre et Montmartre.

                        Le pass Navigo est recommandé
                        pour les déplacements.
                        """),
                        new Document(
                                """
                        Tokyo est réputée pour les quartiers
                        Shibuya, Shinjuku et Akihabara.
                        """)));
    }*/
}
