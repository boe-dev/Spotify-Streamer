package de.boe_dev.spotifystreamer;

/**
 * Created by benny on 14.06.15.
 */
public class ArtistModel {

    private String id, imageUrl, name;

    public ArtistModel(String id, String imageUrl, String name) {

        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

}
