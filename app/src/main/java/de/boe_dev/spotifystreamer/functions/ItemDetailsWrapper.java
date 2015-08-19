package de.boe_dev.spotifystreamer.functions;

import java.io.Serializable;
import java.util.ArrayList;

import de.boe_dev.spotifystreamer.TopTrackModel;

/**
 * Created by ben on 14.08.15.
 */
public class ItemDetailsWrapper implements Serializable {

    //private static final long serialVersionUID = 1L;
    private ArrayList<TopTrackModel> itemDetails;

    public ItemDetailsWrapper(ArrayList<TopTrackModel> items) {
        this.itemDetails = items;
    }

    public ArrayList<TopTrackModel> getItemDetails() {
        return itemDetails;
    }
}