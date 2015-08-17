package de.boe_dev.spotifystreamer;

import java.io.Serializable;

/**
 * Created by benny on 18.06.15.
 */
public class TopTrackModel implements Serializable{

    private String id, artist, imageUrl, name, album, previewUrl;

    public TopTrackModel(String id, String artist, String imageUrl, String name, String album, String previewUrl) {
        this.id = id;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.name = name;
        this.album = album;
        this.previewUrl = previewUrl;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
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

    public String getPreviewUrl() {
        return previewUrl;
    }
}
