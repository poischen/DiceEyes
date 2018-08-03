package acb.diceeyes.View;

import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.ImageView;

/**
 * Created by anita_000 on 03.08.2018.
 */

public class PhotoItem {

    private Bitmap scaledPicture;
    private String absolutePath;
    private ImageView imageView;
    private CheckBox checkbox;
    private boolean taggedToDelete = false;
    private String pictureName;

    public PhotoItem(Bitmap image, String path, String pictureName) {
        super();
        this.scaledPicture = image;
        this.absolutePath = path;
        this.pictureName = pictureName;
    }

    public Bitmap getPicture() {
        return scaledPicture;
    }

    public void setPicture(Bitmap image) {
        this.scaledPicture = image;
    }

    public String getAbsolutePath(){
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath){
        this.absolutePath = absolutePath;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public boolean isTaggedToDelete() {
        return taggedToDelete;
    }

    public void setTaggedToDelete(boolean taggedToDelete) {
        this.taggedToDelete = taggedToDelete;
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(CheckBox checkbox) {
        this.checkbox = checkbox;
    }

    public String getPictureName() {
        return pictureName;
    }

}
