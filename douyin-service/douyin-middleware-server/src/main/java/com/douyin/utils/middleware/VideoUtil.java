package com.douyin.utils.middleware;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <h5>功能:视频封面截帧处理工具类</h5>
 */
@Slf4j
public class VideoUtil {

    // 获取要取得的帧数
    private static final int GET_FRAMES_LENGTH = 5;

    public static InputStream getScreenshot(String videoPath) {
        FFmpegFrameGrabber grabber;
        try {
            grabber = FFmpegFrameGrabber.createDefault(videoPath);
            grabber.start();
            // 视频总帧数
            int videoLength = grabber.getLengthInFrames();
            Frame frame = null;
            int i = 0;
            while (i < videoLength) {
                // 过滤前5帧,避免出现全黑的图片,依自己情况而定(每循环一次取一帧)
                frame = grabber.grabFrame();
                if ((i > GET_FRAMES_LENGTH) && (frame.image != null)) {
                    break;
                }
                i++;
            }
            // 视频旋转度
            String rotate = grabber.getVideoMetadata("rotate");
            Java2DFrameConverter converter = new Java2DFrameConverter();
            // 绘制图片
            BufferedImage bi = converter.getBufferedImage(frame);
            if (rotate != null) {
                // 旋转图片
                bi = rotate(bi, Integer.parseInt(rotate));
            }
            return bufferedImageToInputStream(bi,"jpg");
        } catch (IOException e) {
            log.error("视频信息帧数处理发生异常 [{}]", e.getMessage());
            //e.printStackTrace();
        }
       return null;
    }

    /**
     * 将BufferedImage转换为InputStream
     *
     * @param image 转换流图片
     */
    public static InputStream bufferedImageToInputStream(BufferedImage image, String imgType) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, imgType, os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            return input;
        } catch (IOException e) {
            log.error("提示:", e);
        }
        return null;
    }

    // ==================== private method ====================

    /**
     * <h5>功能:根据视频旋转度来调整图片</h5>
     *
     * @param src   捕获的图像
     * @param angel 视频旋转度
     * @return BufferedImage
     */
    private static BufferedImage rotate(BufferedImage src, int angel) {
        int src_width = src.getWidth(null);
        int src_height = src.getHeight(null);
        int type = src.getColorModel().getTransparency();
        Rectangle rect_des = calcRotatedSize(new Rectangle(new Dimension(src_width, src_height)), angel);
        BufferedImage bi = new BufferedImage(rect_des.width, rect_des.height, type);
        Graphics2D g2 = bi.createGraphics();
        g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return bi;
    }

    /**
     * <h5>功能:计算图片旋转大小</h5>
     *
     * @param src   屏幕坐标中捕获的矩形区域
     * @param angel 视频旋转度
     * @return
     */
    private static Rectangle calcRotatedSize(Rectangle src, int angel) {
        if (angel >= 90) {
            if (angel / 90 % 2 == 1) {
                int temp = src.height;
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % 90;
        }
        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;
        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;
        double angel_dalta_width = Math.atan((double) src.height / src.width);
        double angel_dalta_height = Math.atan((double) src.width / src.height);
        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        int des_width = src.width + len_dalta_width * 2;
        int des_height = src.height + len_dalta_height * 2;
        return new java.awt.Rectangle(new Dimension(des_width, des_height));
    }
}