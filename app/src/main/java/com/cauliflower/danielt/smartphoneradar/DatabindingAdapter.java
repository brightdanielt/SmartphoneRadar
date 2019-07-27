package com.cauliflower.danielt.smartphoneradar;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.facebook.Profile;
import com.squareup.picasso.Picasso;

public class DatabindingAdapter {

    public DatabindingAdapter() {
    loadImage(null, Profile.getCurrentProfile().getProfilePictureUri(100,100),null);
    }

    @BindingAdapter({"imageUrl","error"})

    public static void loadImage(ImageView imageView, Uri uri, Drawable error){
        Picasso.get().load(uri).error(error).into(imageView);
    }
}
