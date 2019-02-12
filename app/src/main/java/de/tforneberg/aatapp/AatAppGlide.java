package de.tforneberg.aatapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public class AatAppGlide extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context,@NonNull GlideBuilder builder) {
        //enables transparent images after transformation
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888));
    }
}