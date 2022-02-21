package com.gdudes.app.gdudesapp.Helpers.ImageHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;

import com.gdudes.app.gdudesapp.Helpers.GDGenericHelper;
import com.gdudes.app.gdudesapp.Helpers.GDLogHelper;
import com.gdudes.app.gdudesapp.Helpers.StringEncoderHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageHelper {
    public static Bitmap GetBitmapFromString(String PicSrc) {
        return GetBitmapFromString(PicSrc, false);
    }

    public static Bitmap GetBitmapFromString(String PicSrc, Boolean ScaleDownTo2048) {
        try {
            byte[] decodedString = Base64.decode(PicSrc.substring(23, PicSrc.length()), Base64.DEFAULT);
            if (ScaleDownTo2048) {
                return scaleDownTo2048(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            } else {
                return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static Bitmap scaleDownTo2048(Bitmap realImage) {
        if (realImage == null) {
            return realImage;
        }
        float maxImageSize = 2408;
        if (realImage.getHeight() > maxImageSize || realImage.getWidth() > maxImageSize) {
            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            int height = Math.round((float) ratio * realImage.getHeight());

            Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, true);
            return newBitmap;
        } else {
            return realImage;
        }
    }

    public static String GetStringFromBitmap(Bitmap image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            temp = "data:image/jpeg;base64," + temp;
            return temp;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return "";
    }

    public static Bitmap GetBitmapFromPath(String photoPath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(photoPath, options);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return null;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int width, int height) {
        try {
            return Bitmap.createScaledBitmap(image, width, height, true);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return null;
    }

    public static Bitmap getBitmapForResource(Resources resources, int ResouceID) {
        try {
            return BitmapFactory.decodeResource(resources, ResouceID);
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return null;
    }

    public static Drawable getDrawableForResource(Resources resources, int ResouceID, Resources.Theme theme) {
        return ResourcesCompat.getDrawable(resources, ResouceID, theme);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return resources.getDrawable(ResouceID, theme);
//        } else {
//            return resources.getDrawable(ResouceID);
//        }
    }

    public BitmapDrawable writeOnDrawable(Resources resources, int drawableId, String text) {
        Bitmap bm = BitmapFactory.decodeResource(resources, drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, bm.getHeight() / 2, paint);
        return new BitmapDrawable(resources, bm);
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // Convert a view to Drawable
    public static Drawable createDrawableFromView(Context context, Resources resources, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return new BitmapDrawable(resources, bitmap);
    }

    public static Drawable GetDrawableFromBitmap(Resources resources, Bitmap bitmap) {
        return new BitmapDrawable(resources, bitmap);
    }


    //Functions used for direct phone image message - START
    public static final int RECEIVED_IMAGE = 0;
    public static final int UPLOAD_IMAGE = 1;
    public static final int SEND_IMAGE = 2;

    public static Boolean SaveImageAtPath(Bitmap image, String filePath) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File f = new File(filePath);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            return true;
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
        return false;
    }

    public static String SaveImageAndGetCompressPicSrc(Bitmap image, String filePath) {
        if (ImageHelper.SaveImageAtPath(image, filePath)) {
            return ImageHelper.SuperCompressPicSrc(filePath);
        }
        return "";
    }

    public static String MakeDirectoryAndSaveImage(String PicSrc, int directionCode) {
        String FilePath = "";
        try {
            FilePath = GetNewFilePath(directionCode);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Bitmap bitmap = GetBitmapFromString(StringEncoderHelper.decodeURIComponent(PicSrc));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File f = new File(FilePath);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            FilePath = "";
        }
        return FilePath;
    }

    public static String MakeDirectoryAndSaveImage(Bitmap PicBitmap, int directionCode) {
        String FilePath = "";
        try {
            FilePath = GetNewFilePath(directionCode);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PicBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File f = new File(FilePath);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            FilePath = "";
        }
        return FilePath;
    }

    public static String GetFileNameDirectoryForImage(int code, String name) {
        //Code : 0 received, 1 upload, 2 sent
        String sDirectory = "";
        try {
            sDirectory = Environment.getExternalStorageDirectory() + File.separator + ".GDudes Images";
            MakeDirectoryIfNeeded(sDirectory);
            switch (code) {
                case RECEIVED_IMAGE:
                    return sDirectory + File.separator + name + ".jpg";
                case UPLOAD_IMAGE:
                    sDirectory = sDirectory + File.separator + "Uploaded" + File.separator + name + ".jpg";
                    MakeDirectoryIfNeeded(sDirectory);
                    break;
                case SEND_IMAGE:
                    sDirectory = sDirectory + File.separator + "Sent" + File.separator + name + ".jpg";
                    MakeDirectoryIfNeeded(sDirectory);
                    break;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            sDirectory = "";
        }
        return sDirectory;
    }

    public static String CreateDirectoryForImage(int code) {
        //Code : 0 received, 1 upload, 2 sent
        String sDirectory = "";
        try {
            sDirectory = Environment.getExternalStorageDirectory() + File.separator + ".GDudes Images";
            MakeDirectoryIfNeeded(sDirectory);
            switch (code) {
                case RECEIVED_IMAGE:
                    return sDirectory;
                case UPLOAD_IMAGE:
                    sDirectory = sDirectory + File.separator + "Uploaded";
                    MakeDirectoryIfNeeded(sDirectory);
                    break;
                case SEND_IMAGE:
                    sDirectory = sDirectory + File.separator + "Sent";
                    MakeDirectoryIfNeeded(sDirectory);
                    break;
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            sDirectory = "";
        }
        return sDirectory;
    }

    private static void MakeDirectoryIfNeeded(String directory) {
        try {
            File fileDirectory = new File(directory);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
        }
    }

    public static String GetNewFilePath(int code) {
        String Directory = "";
        String NewFilePath = "";
        try {
            switch (code) {
                case 0:
                    Directory = CreateDirectoryForImage(0);
                    break;
                case 1:
                    Directory = CreateDirectoryForImage(1);
                    break;
                case 2:
                    Directory = CreateDirectoryForImage(2);
                    break;
            }
            NewFilePath = Directory + File.separator + "GDudes_" + GDGenericHelper.GetNewGUID() + ".jpg";
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return NewFilePath;
    }


    public static Bitmap CompressImageFile(String FilePath, Boolean compress, Boolean DoubleCompression) {
        Bitmap thumbnail = null;
        try {
            Pair<Integer, Integer> QualityAndSampleSize = GetCompressImgQualityAndSampleSize(FilePath, DoubleCompression);
            if (compress) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                options.inSampleSize = QualityAndSampleSize.second;
                thumbnail = ImageHelper.scaleDownTo2048(BitmapFactory.decodeFile(FilePath, options));
            } else {
                thumbnail = ImageHelper.scaleDownTo2048(DecodeImageFile(FilePath));
            }

//            ImageView img = new ImageView(context);
//            img.setImageBitmap(thumbnail);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return thumbnail;
    }

    public static String CompressImageFileGetString(String FilePath, Boolean compress, Boolean DoubleCompression) {
        Bitmap thumbnail = CompressImageFile(FilePath, compress, DoubleCompression);
        String PicSrc = "";
        if (thumbnail != null) {
            PicSrc = ImageHelper.GetStringFromBitmap(thumbnail);
        }
        return PicSrc;
    }

    private static Pair<Integer, Integer> GetCompressImgQualityAndSampleSize(String FilePath, Boolean DoubleCompression) {
        Pair<Integer, Integer> QualityAndSampleSize = new Pair<>(70, 2);
        try {
            Bitmap thumbnail = ImageHelper.scaleDownTo2048(DecodeImageFile(FilePath));
            String PicSrc = GetStringFromBitmap(thumbnail);
//            File file = new File(FilePath);
//            long size = file.length();
            long size = PicSrc.length();
            if (size <= 300000) {
                // less than 300 KB
                QualityAndSampleSize = new Pair<>(100, 1);
            } else if (size <= 500000) {
                // less than 500 KB
                QualityAndSampleSize = new Pair<>(100, 1);
            } else if (size > 500000 && size <= 1000000) {
                //500 KB - 1 MB
                QualityAndSampleSize = new Pair<>(100, DoubleCompression ? 1 : 1);
            } else if (size > 1000000 && size <= 2000000) {
                //1 - 2 MB
                QualityAndSampleSize = new Pair<>(100, DoubleCompression ? 2 : 1);
            } else if (size > 2000000 && size <= 3000000) {
                //2 - 3 MB
                QualityAndSampleSize = new Pair<>(35, DoubleCompression ? 2 : 2);
            } else if (size > 3000000 && size <= 5000000) {
                //3 - 5 MB
                QualityAndSampleSize = new Pair<>(35, DoubleCompression ? 4 : 2);
            } else if (size > 5000000 && size <= 10000000) {
                //5 - 10 MB
                QualityAndSampleSize = new Pair<>(30, DoubleCompression ? 8 : 4);
            } else if (size > 10000000 && size <= 20000000) {
                //10 - 20 MB
                QualityAndSampleSize = new Pair<>(30, DoubleCompression ? 16 : 8);
            } else if (size > 20000000) {
                // more than 20 MB
                QualityAndSampleSize = new Pair<>(30, DoubleCompression ? 32 : 16);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return QualityAndSampleSize;
    }

    public static String SuperCompressPicSrc(String FilePath) {
        String CompressedImage = "";
        try {
            Pair<Integer, Integer> ImgQualityAndSampleSize = GetCompressImgQualityAndSampleSize(FilePath, false);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = ImgQualityAndSampleSize.second * 8;
            Bitmap thumbnail = BitmapFactory.decodeFile(FilePath, options);
            CompressedImage = StringEncoderHelper.encodeURIComponent(ImageHelper.GetStringFromBitmap(thumbnail));
        } catch (Exception ex) {
            GDLogHelper.LogException(ex);
            CompressedImage = "";
        }
        return CompressedImage;
    }

    public static Boolean CopyImage(String source, String destination) {
        Boolean Copied = false;
        try {
            File sourceFile = new File(source);
            File destinationFile = new File(destination);
            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            // write the output file
            out.flush();
            out.close();
            out = null;
            Copied = true;
        } catch (Exception e) {
            e.printStackTrace();
            GDLogHelper.LogException(e);
        }
        return Copied;
    }

    public static Boolean DeleteImageFile(String source) {
        Boolean Deleted = false;
        try {
            File sourceFile = new File(source);
            Deleted = sourceFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return Deleted;
    }
    //Functions used for direct phone image message - END

    private static Bitmap DecodeImageFile(String imagePath) {
        try {
            return BitmapFactory.decodeFile(imagePath);
        } catch (OutOfMemoryError err1) {
            try {
                BitmapFactory.Options options;
                options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                return BitmapFactory.decodeFile(imagePath, options);
            } catch (OutOfMemoryError err2) {
                try {
                    BitmapFactory.Options options;
                    options = new BitmapFactory.Options();
                    options.inSampleSize = 3;
                    return BitmapFactory.decodeFile(imagePath, options);
                } catch(Exception excepetion) {
                    GDLogHelper.LogException(excepetion);
                }
            }
        }
        return null;
    }

    public static String DeleteAllUploadedImges() {
        String FilePath = "";
        try {
            FilePath = CreateDirectoryForImage(UPLOAD_IMAGE);
            File directory = new File(FilePath);
            File files[] = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = new File(FilePath + File.separator + files[i].getName());
                file.delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            FilePath = "";
        }
        return FilePath;
    }

    public static Boolean FileExists(String filePath) {
        return new File(filePath).exists();
    }
}
