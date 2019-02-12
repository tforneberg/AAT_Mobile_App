package de.tforneberg.aatapp.repo;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.data.AatDatabase;
import de.tforneberg.aatapp.data.dao.ImageDao;
import de.tforneberg.aatapp.data.dao.ImageSetDao;
import de.tforneberg.aatapp.data.dao.UserDao;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.model.ImageSetImageConnection;
import de.tforneberg.aatapp.model.User;

public class ImageRepository {

    private ImageDao imageDao;
    private ImageSetDao imageSetDao;

    ImageRepository(AatDatabase database) {
        imageDao = database.imageDao();
        imageSetDao = database.imageSetDao();
    }

    //INTERFACES -----------------------
    public interface ImageAddedListener {
        void onImageAdded(boolean success);
    }

    // GET METHODS
    public LiveData<Image> getImageById(long id) {
        return imageDao.getImageById(id);
    }

    public int getIdFromDefaultImage(Context context, Image image) {
        String pkg = context.getPackageName();
        return context.getResources().getIdentifier(image.getPath(), "drawable", pkg);
    }

    public File getFileFromImage(Context context, Image image) {
        return new File(context.getFilesDir() + "/" + image.getPath());
    }

    public LiveData<List<Image>> getAllImages() { return imageDao.getAllImages(); }

    public LiveData<List<Image>> getImagesByImageSetId(long id) { return imageDao.getImagesByImageSetId(id); }

    public List<Image> getImagesByImageSetIdSync(long id) { return imageDao.getImagesByImageSetIdSync(id); }

    public LiveData<ImageSet> getImageSetById(long id) {
        return imageSetDao.getImageSetById(id);
    }

    public ImageSet getImageSetByIdSync(long id) {
        return imageSetDao.getImageSetByIdSync(id);
    }

    public LiveData<ImageSet> getActiveImageSet() { return imageSetDao.getActiveImageSet(); }

    public LiveData<List<ImageSet>> getAllImageSets() { return imageSetDao.getAllImageSets(); }

    //public ADD image method
    public void addImage(Context context, Uri uri, String filename, String type, boolean push,
                         ImageSet imageSet, ImageAddedListener listener) {
        new AddImageAsyncTask(context, uri, filename, type, this, push, imageSet, listener).execute();
    }

    //INSERT methods
    private void insertImage(Image image, ImageSet imageSet) {
        new InsertImageAsyncTask(imageDao, imageSet).execute(image);
    }

    public void insertImageSet(ImageSet imageSet) {
        new InsertImageSetAsyncTask(imageSetDao).execute(imageSet);
    }

    //UPDATE methods
    public void updateImageSets(ImageSet... imageSets) {
        new UpdateImageSetAsyncTask(imageSetDao).execute(imageSets);
    }

    public void updateImages(Image... images) {
        new UpdateImageAsyncTask(imageDao).execute(images);
    }

    public void setNewActiveImageSet(ImageSet newActive, ImageSet oldActive) {
        if (oldActive != null) {
            new UpdateActiveImageSetAsyncTask(imageSetDao, newActive).execute(oldActive);
        } else {
            new UpdateImageSetAsyncTask(imageSetDao).execute(newActive);
        }
    }

    //DELETE METHODS
    public void deleteImage(Image image, Context context) {
        context.deleteFile(image.getPath());
        new DeleteImageAsyncTask(imageDao).execute(image);
    }

    public void deleteImageSet(Activity activity, ImageSet imageSet) {
        new DeleteImageSetAsyncTask(activity, imageSetDao).execute(imageSet);
    }

    //async add image task
    static class AddImageAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String filename;
        private String type;
        private File filesDir;
        private ImageRepository repo;
        private boolean push;
        private ImageSet imageSet;
        private ImageAddedListener listener;
        private Uri uri;
        private Bitmap bitmap;

        AddImageAsyncTask(Context context, Uri uri, String filename, String type, ImageRepository repo, boolean push, ImageSet imageSet, ImageAddedListener listener) {
            this.repo = repo;
            this.filesDir = context.getFilesDir();
            this.filename = filename;
            this.type = type;
            this.push = push;
            this.imageSet = imageSet;
            this.listener = listener;
            this.uri = uri;

            try {
                this.bitmap = getBitmapFromUri(context);
            } catch (Exception e) {
                this.bitmap = null;
            }
        }

        private Bitmap getBitmapFromUri(Context context) throws Exception {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) { return null; }

            BitmapFactory.Options dbo = new BitmapFactory.Options();
            dbo.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, dbo);
            is.close();

            //get the rotation metadata of the given image file
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
            int rotation;
            int rotatedWidth, rotatedHeight;
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                rotation = cursor.getInt(0);
                cursor.close();

                if (rotation == 90 || rotation == 270) {
                    //noinspection SuspiciousNameCombination
                    rotatedWidth = dbo.outHeight;
                    //noinspection SuspiciousNameCombination
                    rotatedHeight = dbo.outWidth;
                } else {
                    rotatedWidth = dbo.outWidth;
                    rotatedHeight = dbo.outHeight;
                }

                //get the actual bitmap file
                Bitmap bitmap;
                is = context.getContentResolver().openInputStream(uri);
                if (rotatedWidth > 4000 || rotatedHeight > 4000) {
                    //scale down if too large
                    float widthRatio = ((float) rotatedWidth) / ((float) 4000);
                    float heightRatio = ((float) rotatedHeight) / ((float) 4000);
                    float maxRatio = Math.max(widthRatio, heightRatio);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = (int) maxRatio;
                    bitmap = BitmapFactory.decodeStream(is, null, options);
                } else {
                    bitmap = BitmapFactory.decodeStream(is);
                }
                if (is != null) { is.close(); }
                if (bitmap == null) { return null; }

                if (rotation > 0) {
                    //original image has rotation --> do also rotate the new image
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                return bitmap;
            }
            return null;
        }

        protected Boolean doInBackground(Void... voids) {
            if (bitmap == null) {
                return false;
            }

            //Choose new data type from old data type, abort if data type is not supported
            String oldType = type.substring(type.lastIndexOf("/") + 1);
            String newType;
            Bitmap.CompressFormat compressFormat;
            if (oldType.equals("png") || oldType.equals("gif")) {
                compressFormat = Bitmap.CompressFormat.PNG;
                newType = ".png";
            } else if (oldType.equals("bmp") || oldType.equals("jpeg") || oldType.contains("tif")) {
                compressFormat = Bitmap.CompressFormat.JPEG;
                newType = ".jpeg";
            } else {
                return false;
            }

            //get unique new filename
            String oldFilename = filename;
            File newFile;
            do {
                filename = oldFilename;
                filename += "_" + System.nanoTime() + newType;
                newFile = new File(filesDir, filename);
            } while (newFile.exists());

            //write to disk
            try {
                FileOutputStream out = new FileOutputStream(newFile);
                bitmap.compress(compressFormat, 90, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                return false;
            }

            //insert into database
            repo.insertImage(new Image(filename, push), imageSet);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (listener != null) {
                listener.onImageAdded(success);
            }
        }
    }

    //async insert tasks
    static class InsertUserAsyncTask extends AsyncTask<User, Void, Long> {
        private UserDao dao;
        InsertUserAsyncTask(UserDao dao) { this.dao = dao; }

        @Override
        protected Long doInBackground(final User... params) {
            return dao.insertUser(params[0]);
        }
    }

    static class InsertImageAsyncTask extends AsyncTask<Image, Void, Long> {
        private ImageDao dao;
        private ImageSet imageSet;
        InsertImageAsyncTask(ImageDao dao,  ImageSet imageSet) {
            this.dao = dao;
            this.imageSet = imageSet;
        }

        @Override
        protected Long doInBackground(final Image... params) {
            return dao.insertImage(params[0]);
        }

        @Override
        protected void onPostExecute(Long resultId) {
            super.onPostExecute(resultId);
            ImageSetImageConnection conn = new ImageSetImageConnection(Long.toString(imageSet.getId()), Long.toString(resultId));
            new ImageRepository.InsertImageSetImageAsyncTask(dao).execute(conn);
        }
    }

    private static class InsertImageSetImageAsyncTask extends AsyncTask<ImageSetImageConnection, Void, Void> {
        private ImageDao dao;
        InsertImageSetImageAsyncTask(ImageDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(final ImageSetImageConnection... params) {
            dao.insertImageSetImageConnection(params[0]);
            return null;
        }
    }

    static class InsertImageSetAsyncTask extends AsyncTask<ImageSet, Void, Void> {
        private ImageSetDao dao;
        InsertImageSetAsyncTask(ImageSetDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(final ImageSet... params) {
            dao.insertImageSet(params[0]);
            return null;
        }
    }

    //Async update tasks
    static class UpdateImageAsyncTask extends AsyncTask<Image, Void, Void> {
        private ImageDao dao;
        UpdateImageAsyncTask(ImageDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(final Image... params) {
            dao.updateImages(params[0]);
            return null;
        }
    }

    static class UpdateImageSetAsyncTask extends AsyncTask<ImageSet, Void, Void>{
        private ImageSetDao dao;
        UpdateImageSetAsyncTask(ImageSetDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(ImageSet... imageSets) {
            dao.updateImageSets(imageSets);
            return null;
        }
    }

    static class UpdateActiveImageSetAsyncTask extends AsyncTask<ImageSet, Void, Void>{
        private ImageSetDao dao;
        private ImageSet newActive;
        UpdateActiveImageSetAsyncTask(ImageSetDao dao, ImageSet newActive) {
            this.dao = dao;
            this.newActive = newActive;
        }

        @Override
        protected Void doInBackground(ImageSet... oldActive) {
            dao.updateImageSets(oldActive);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new UpdateImageSetAsyncTask(dao).execute(newActive);
        }
    }

    //Async delete tasks
    static class DeleteImageAsyncTask extends AsyncTask<Image, Void, Void> {
        private ImageDao dao;
        DeleteImageAsyncTask(ImageDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(Image... params) {
            dao.deleteImages(params);
            return null;
        }
    }

    static class DeleteImageSetAsyncTask extends AsyncTask<ImageSet, Void, Boolean> {
        private ImageSetDao dao;
        private WeakReference<Activity> activity;

        DeleteImageSetAsyncTask(Activity activity, ImageSetDao dao) {
            this.activity = new WeakReference<>(activity);
            this.dao = dao;
        }

        @Override
        protected Boolean doInBackground(ImageSet... imageSets) {
            try {
                dao.deleteImageSets(imageSets);
                return true;
            } catch (SQLiteConstraintException e) {
                return false;
             }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toast.makeText(activity.get(), activity.get().getString(R.string.err_delete_image_set), Toast.LENGTH_LONG).show();
            }
        }
    }
}
