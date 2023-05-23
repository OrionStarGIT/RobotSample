package com.ainirobot.robotos.maputils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.ainirobot.base.analytics.utils.Md5Util;
import com.ainirobot.coreservice.client.Definition;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class MapppUtils {

    public final static int PASS = 0xFF4E75C0;
    public final static int BLOCK = 0xFF1D3C7F;
    public final static int UNDETECT = 0xFF182A52;
    public final static int OBSTACLE = 0xFF1D3C7E;
    private final static String MAP_PGM_NAME = "map.pgm";
    private static final String TAG = "MapppUtils";

    /**
     * 将bitmap转换为本地的图片
     *
     * @param bitmap
     * @return
     */

    public static String bitmap2Path(Bitmap bitmap, String path) {
        try {
            OutputStream os = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            os.flush();

            os.close();

        } catch (Exception e) {
            Log.e("TAG", "", e);

        }

        return path;

    }

    public static Pose2d pose2PixelByRoverMap(RoverMap costMap, Pose2d pose) {
        Pose2d newPose = new Pose2d(pose.x, pose.y, pose.t, pose.status);
        if (costMap != null) {
            newPose.x -= costMap.x;
            newPose.x /= costMap.res;
            newPose.y -= costMap.y;
            newPose.y /= costMap.res;
            newPose.y = costMap.height - newPose.y;
        }
        return newPose;
    }

    /*
    * 适用于新的地图结构，读取pgm文件
    * For new pgm map structure.
    * */
    public static RoverMap loadMapNew(String pgmPath){
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        try {


            RoverMap roverMap = new RoverMap();
            fileInputStream = new FileInputStream(pgmPath);
            dataInputStream = new DataInputStream(fileInputStream);

            String magic = nextNonCommentLine(dataInputStream);
            Log.d("PgmBitMap", magic);
            if (!magic.equals("P5")) {
                throw new Exception("Unknown magic number: " + magic);
            }

            String widthHeight = nextNonCommentLine(dataInputStream);
            String[] tokens = widthHeight.split(" ");
            int width = Integer.parseInt(tokens[0]);
            int height = Integer.parseInt(tokens[1]);
            int size = width * height;

            nextNonCommentLine(dataInputStream);
            /*String sMaxVal = nextNonCommentLine(dataInputStream);
            int maxVal = Integer.parseInt(sMaxVal);*/

            byte[] pixelsByte = new byte[size];
            dataInputStream.read(pixelsByte, 0, size);
            roverMap.extra = new byte[16];
            dataInputStream.read(roverMap.extra, 0, 16);

            int[] pixelsInt = new int[size];
            for (int i = 0; i < size; i++) {
                int p = pixelsByte[i] & 0xff;
                switch (p) {
                    case 0x96:
                        pixelsInt[i] = UNDETECT;//灰色---白色
                        break;
                    case 0x00:
                        pixelsInt[i] = BLOCK;//黑色---深蓝
                        break;
                    case 0xff:
                        pixelsInt[i] = PASS;//白色---浅蓝
                        break;
                    case 0x05:
                        pixelsInt[i] = OBSTACLE;
                        break;
                    default:
                        break;
                }
                //pixelsInt[i] = 0xff000000 | (p << 16) | (p << 8) | p;
            }

            roverMap.x = byte2float(roverMap.extra, 8);
            roverMap.y = byte2float(roverMap.extra, 12);
            roverMap.res = bytes2Double(roverMap.extra, 0);
            roverMap.height = height;
            roverMap.width = width;

            roverMap.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            roverMap.bitmap.setPixels(pixelsInt, 0, width, 0, 0, width, height);
            return roverMap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(dataInputStream);
            IOUtils.close(fileInputStream);
        }
        return null;
    }
    /*
     * 适用于老的地图结构，读取pgm文件
     * For old pgm map structure.
     * */
    public static RoverMap loadMap(String path) {
        File zipFile = new File(path);
        String pgmPath = zipFile.getParent() + File.separator + MAP_PGM_NAME;
        File tempPgm = new File(pgmPath);
        try {
            upZipFile(zipFile, zipFile.getParent(), MAP_PGM_NAME);
        } catch (IOException e) {
            if (tempPgm.exists()) {
                tempPgm.delete();
            }
            e.printStackTrace();
            return null;
        }

        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        try {

            RoverMap roverMap = new RoverMap();
            roverMap.pgmMd5= Md5Util.getFileMD5(tempPgm);
            fileInputStream = new FileInputStream(pgmPath);
            dataInputStream = new DataInputStream(fileInputStream);

            String magic = nextNonCommentLine(dataInputStream);
            Log.d("PgmBitMap", magic);
            if (!magic.equals("P5")) {
                throw new Exception("Unknown magic number: " + magic);
            }

            String widthHeight = nextNonCommentLine(dataInputStream);
            String[] tokens = widthHeight.split(" ");
            int width = Integer.parseInt(tokens[0]);
            int height = Integer.parseInt(tokens[1]);
            int size = width * height;

            nextNonCommentLine(dataInputStream);
            /*String sMaxVal = nextNonCommentLine(dataInputStream);
            int maxVal = Integer.parseInt(sMaxVal);*/

            byte[] pixelsByte = new byte[size];
            dataInputStream.read(pixelsByte, 0, size);
            roverMap.extra = new byte[16];
            dataInputStream.read(roverMap.extra, 0, 16);

            int[] pixelsInt = new int[size];
            for (int i = 0; i < size; i++) {
                int p = pixelsByte[i] & 0xff;
                switch (p) {
                    case 0x96:
                        pixelsInt[i] = UNDETECT;//灰色---白色
                        break;
                    case 0x00:
                        pixelsInt[i] = BLOCK;//黑色---深蓝
                        break;
                    case 0xff:
                        pixelsInt[i] = PASS;//白色---浅蓝
                        break;
                    case 0x05:
                        pixelsInt[i] = OBSTACLE;
                        break;
                    default:
                        break;
                }
                //pixelsInt[i] = 0xff000000 | (p << 16) | (p << 8) | p;
            }

            roverMap.x = byte2float(roverMap.extra, 8);
            roverMap.y = byte2float(roverMap.extra, 12);
            roverMap.res = bytes2Double(roverMap.extra, 0);
            roverMap.height = height;
            roverMap.width = width;

            roverMap.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            roverMap.bitmap.setPixels(pixelsInt, 0, width, 0, 0, width, height);
            return roverMap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(dataInputStream);
            IOUtils.close(fileInputStream);
            if (tempPgm.exists()) {
                tempPgm.delete();
            }
        }
        return null;
    }


    private static final int ZIP_BUFF_SIZE = 1024 * 1024; // 1M Byte

    public static void upZipFile(File zipFile, String folderPath, String matchFileName)
            throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = null;
        try {
            zf = new ZipFile(zipFile);
            for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    ZipEntry entry = ((ZipEntry) entries.nextElement());
                    //判断如果当前要解压的文件名和需要解压的文件名不一致，则继续查询
                    if (!TextUtils.isEmpty(matchFileName)) {
                        if (entry == null || !matchFileName.equals(entry.getName())) {
                            continue;
                        }
                    }
                    in = zf.getInputStream(entry);
                    String str = folderPath + File.separator + entry.getName();
                    Log.d("MapUtils", str);
                    //str = new String(str.getBytes("8859_1"), "GB2312");
                    Log.d("MapUtils", "**** " + new String(str.getBytes("8859_1"), "GB2312"));
                    File desFile = new File(str);
                    if (!desFile.exists()) {
                        File fileParentDir = desFile.getParentFile();
                        if (!fileParentDir.exists()) {
                            fileParentDir.mkdirs();
                        }
                        desFile.createNewFile();
                    }
                    out = new FileOutputStream(desFile);
                    byte buffer[] = new byte[ZIP_BUFF_SIZE];
                    int realLength;
                    while ((realLength = in.read(buffer)) > 0) {
                        out.write(buffer, 0, realLength);
                    }
                } finally {
                    IOUtils.close(in);
                    IOUtils.close(out);
                }
            }
        } finally {
            IOUtils.close(zf);
        }
    }

    private static String nextAnyLine(DataInputStream dataInputStream) throws IOException {
        StringBuffer sb = new StringBuffer();
        byte b = 0;
        while (b != 10) // newline
        {
            b = dataInputStream.readByte();
            char c = (char) b;
            sb.append(c);
        }
        return sb.toString().trim();
    }

    private static String nextNonCommentLine(DataInputStream dataInputStream) throws IOException {
        String s = nextAnyLine(dataInputStream);
        while (s.startsWith("#") || s.equals("")) {
            s = nextAnyLine(dataInputStream);
        }
        return s;
    }

    /**
     * 文字生成图片
     *
     * @param text
     * @param textSize
     * @param textColor
     * @param bgColor
     * @param padding
     * @return
     */
    public static Bitmap text2Bitmap(String text, int textSize, String textColor, String bgColor, int padding) {
        Paint paint = new Paint();

        paint.setColor(Color.parseColor(textColor));

        paint.setTextSize(textSize);

        paint.setStyle(Paint.Style.FILL);

        paint.setAntiAlias(true);

        float width = paint.measureText(text, 0, text.length());

        float top = paint.getFontMetrics().top;

        float bottom = paint.getFontMetrics().bottom;

        Bitmap bm = Bitmap.createBitmap((int) (width + padding * 2), (int) ((bottom - top) + padding * 2), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bm);

        canvas.drawColor(Color.parseColor(bgColor));

        canvas.drawText(text, padding, -top + padding, paint);

        return bm;

    }

    /**
     * 浮点和double的区别，一个是value类型，一个是字节格个数
     *
     * @param arr
     * @param index
     * @return
     */
    public static float byte2float(byte[] arr, int index) {
        int value = 0;
        for (int i = index; i < index + 4; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * (i - index));
        }
        return Float.intBitsToFloat(value);
    }

    public static double bytes2Double(byte[] arr, int index) {
        long value = 0;
        for (int i = index; i < index + 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * (i - index));
        }
        return Double.longBitsToDouble(value);
    }

    /**
     * 解析 共享内存数据流为 RoverMap
     */
    public static RoverMap loadPFD2RoverMap(FileInputStream fileInputStream) {
        Log.d(TAG, "loadPFD2RoverMap:");
        DataInputStream dataInputStream = null;
        try {
            Log.d(TAG, "loadPFD2RoverMap: available=" + fileInputStream.available());
            RoverMap roverMap = new RoverMap();
            dataInputStream = new DataInputStream(fileInputStream);
            Log.d(TAG, "loadPFD2RoverMap: available=" + dataInputStream.available());

            String magic = nextNonCommentLine(dataInputStream);
            Log.d(TAG, "loadPFD2RoverMap: magic=" + magic);
            if (!magic.equals("P5")) {
                throw new Exception("Unknown magic number: " + magic);
            }

            String widthHeight = nextNonCommentLine(dataInputStream);
            String[] tokens = widthHeight.split(" ");
            int width = Integer.parseInt(tokens[0]);
            int height = Integer.parseInt(tokens[1]);
            int size = width * height;

            nextNonCommentLine(dataInputStream);

            byte[] pixelsByte = new byte[size];
            dataInputStream.read(pixelsByte, 0, size);
            roverMap.extra = new byte[16];
            dataInputStream.read(roverMap.extra, 0, 16);

            int[] pixelsInt = new int[size];
            for (int i = 0; i < size; i++) {
                int p = pixelsByte[i] & 0xff;
                switch (p) {
                    case 0x96:
                        pixelsInt[i] = Definition.MAPCOLOR.UNDETECT;//未探测
                        break;
                    case 0x00:
                        pixelsInt[i] = Definition.MAPCOLOR.BLOCK;//禁行线
                        break;
                    case 0xff:
                        pixelsInt[i] = Definition.MAPCOLOR.PASS;//可通行
                        break;
                    case 0x05:
                        pixelsInt[i] = Definition.MAPCOLOR.OBSTACLE;//障碍物
                        break;
                    default:
                        break;
                }
            }

            roverMap.x = byte2float(roverMap.extra, 8);
            roverMap.y = byte2float(roverMap.extra, 12);
            roverMap.res = bytes2Double(roverMap.extra, 0);
            roverMap.height = height;
            roverMap.width = width;

            roverMap.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            roverMap.bitmap.setPixels(pixelsInt, 0, width, 0, 0, width, height);
            Log.d(TAG, "loadPFD2RoverMap: Done!");
            return roverMap;
        } catch (Exception e) {
            Log.d(TAG, "loadPFD2RoverMap:Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.close(dataInputStream);
            IOUtils.close(fileInputStream);
        }
        return null;
    }

    /**
     * 把 RoverMap 转化成共享内存数据
     */
    public static byte[] saveRoverMapToPFDData(RoverMap map) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(outputStream);

            String fileHeader = String.format("P5\n%d %d\n255\n", map.bitmap.getWidth(), map
                    .bitmap.getHeight());
            dataOutputStream.writeBytes(fileHeader);

            int size = map.bitmap.getWidth() * map.bitmap.getHeight();
            byte[] pixelsByte = new byte[size];
            int[] pixelsInt = new int[size];
            map.bitmap.getPixels(pixelsInt, 0, map.bitmap.getWidth(), 0, 0, map.bitmap.getWidth()
                    , map.bitmap.getHeight());

            for (int i = 0; i < size; i++) {
                switch (pixelsInt[i]) {
                    case Definition.MAPCOLOR.UNDETECT:
                        pixelsInt[i] = 0xff969696;
                        break;
                    case Definition.MAPCOLOR.BLOCK:
                        pixelsInt[i] = 0xff000000;
                        break;
                    case Definition.MAPCOLOR.PASS:
                        pixelsInt[i] = 0xffffffff;
                        break;
                    case Definition.MAPCOLOR.OBSTACLE:
                        pixelsInt[i] = 0xff050505;
                        break;
                    default:
                        break;
                }
                pixelsByte[i] = (byte) (pixelsInt[i] & 0x000000ff);
            }
            dataOutputStream.write(pixelsByte);
            dataOutputStream.write(map.extra);
            dataOutputStream.flush();
            byte[] bytes = outputStream.toByteArray();
            Log.d(TAG, "saveRoverMapToPFDData: Done!");
            return bytes;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "saveRoverMapToPFDData:FileNotFoundException " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "saveRoverMapToPFDData:IOException: " + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.close(dataOutputStream);
            IOUtils.close(outputStream);
        }
        return null;
    }
}


