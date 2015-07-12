package de.boe_dev.spotifystreamer;

/**
 * Created by benny on 18.06.15.
 */
public class TopTrackModel {

    private String id, imageUrl, name, album;

    public TopTrackModel(String id, String imageUrl, String name, String album) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.album = album;
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

    public String getAlbum() {
        return album;
    }
}
