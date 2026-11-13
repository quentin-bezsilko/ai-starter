package com.qbe.aistarter.enums;

public enum TravelProfile {
    SOLO("voyageur seul recherchant liberté et flexibilité"),
    COUPLE("couple recherchant des activités romantiques et des expériences à partager"),
    FAMILY("famille avec enfants privilégiant sécurité et activités familiales"),
    VIP("voyageur haut de gamme recherchant confort, exclusivité et prestations premium"),
    BUSINESS("voyageur d'affaires ayant peu de temps libre"),
    BACKPACKER("voyageur économique privilégiant les auberges et les activités peu coûteuses");

    private final String description;

    TravelProfile(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
