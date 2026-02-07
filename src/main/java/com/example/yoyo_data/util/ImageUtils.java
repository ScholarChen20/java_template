package com.example.yoyo_data.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class ImageUtils {

    private static final String DEFAULT_FORMAT = "jpg";

    private static final int DEFAULT_QUALITY = 85;

    public static BufferedImage read(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        try {
            File file = new File(filePath);
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage read(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage read(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage readFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean write(BufferedImage image, String filePath) {
        if (image == null || filePath == null || filePath.isEmpty()) {
            return false;
        }
        String format = getFormat(filePath);
        try {
            return ImageIO.write(image, format, new File(filePath));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean write(BufferedImage image, String format, File file) {
        if (image == null || format == null || file == null) {
            return false;
        }
        try {
            return ImageIO.write(image, format, file);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean write(BufferedImage image, String format, OutputStream outputStream) {
        if (image == null || format == null || outputStream == null) {
            return false;
        }
        try {
            return ImageIO.write(image, format, outputStream);
        } catch (IOException e) {
            return false;
        }
    }

    public static byte[] toBytes(BufferedImage image, String format) {
        if (image == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        if (image == null || width <= 0 || height <= 0) {
            return image;
        }
        BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage scale(BufferedImage image, double scale) {
        if (image == null || scale <= 0) {
            return image;
        }
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);
        return resize(image, width, height);
    }

    public static BufferedImage scaleByWidth(BufferedImage image, int targetWidth) {
        if (image == null || targetWidth <= 0) {
            return image;
        }
        double scale = (double) targetWidth / image.getWidth();
        return scale(image, scale);
    }

    public static BufferedImage scaleByHeight(BufferedImage image, int targetHeight) {
        if (image == null || targetHeight <= 0) {
            return image;
        }
        double scale = (double) targetHeight / image.getHeight();
        return scale(image, scale);
    }

    public static BufferedImage scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
        if (image == null || maxWidth <= 0 || maxHeight <= 0) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return image;
        }

        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double scale = Math.min(widthRatio, heightRatio);

        return scale(image, scale);
    }

    public static BufferedImage crop(BufferedImage image, int x, int y, int width, int height) {
        if (image == null) {
            return null;
        }
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.min(width, image.getWidth() - x);
        height = Math.min(height, image.getHeight() - y);

        if (width <= 0 || height <= 0) {
            return null;
        }

        return image.getSubimage(x, y, width, height);
    }

    public static BufferedImage cropCenter(BufferedImage image, int width, int height) {
        if (image == null) {
            return null;
        }
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return crop(image, x, y, width, height);
    }

    public static BufferedImage rotate(BufferedImage image, double angle) {
        if (image == null) {
            return null;
        }
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g = rotatedImage.createGraphics();
        g.translate((newWidth - image.getWidth()) / 2.0, (newHeight - image.getHeight()) / 2.0);
        g.rotate(radians, image.getWidth() / 2.0, image.getHeight() / 2.0);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rotatedImage;
    }

    public static BufferedImage flipHorizontal(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, width, 0, -width, height, null);
        g.dispose();
        return flippedImage;
    }

    public static BufferedImage flipVertical(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, 0, height, width, -height, null);
        g.dispose();
        return flippedImage;
    }

    public static BufferedImage grayscale(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    public static BufferedImage addWatermark(BufferedImage image, String watermarkText) {
        return addWatermark(image, watermarkText, Color.WHITE, 0.7f);
    }

    public static BufferedImage addWatermark(BufferedImage image, String watermarkText, Color color, float alpha) {
        if (image == null || watermarkText == null || watermarkText.isEmpty()) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage watermarkedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = watermarkedImage.createGraphics();
        g.drawImage(image, 0, 0, null);

        Font font = new Font("Arial", Font.BOLD, width / 20);
        g.setFont(font);
        g.setColor(color);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(watermarkText);
        int textHeight = metrics.getHeight();

        int x = (width - textWidth) / 2;
        int y = height - textHeight - 20;

        g.drawString(watermarkText, x, y);
        g.dispose();
        return watermarkedImage;
    }

    public static BufferedImage addWatermarkImage(BufferedImage image, BufferedImage watermark) {
        return addWatermarkImage(image, watermark, 0.5f, 10, 10);
    }

    public static BufferedImage addWatermarkImage(BufferedImage image, BufferedImage watermark, float alpha, int x, int y) {
        if (image == null || watermark == null) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage watermarkedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = watermarkedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(watermark, x, y, null);
        g.dispose();
        return watermarkedImage;
    }

    public static BufferedImage convertFormat(BufferedImage image, int format) {
        if (image == null) {
            return null;
        }
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), format);
        Graphics2D g = convertedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return convertedImage;
    }

    public static int getWidth(BufferedImage image) {
        return image == null ? 0 : image.getWidth();
    }

    public static int getHeight(BufferedImage image) {
        return image == null ? 0 : image.getHeight();
    }

    public static String getFormat(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return DEFAULT_FORMAT;
        }
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            return "jpg";
        } else if (extension.equals("png")) {
            return "png";
        } else if (extension.equals("gif")) {
            return "gif";
        } else if (extension.equals("bmp")) {
            return "bmp";
        } else if (extension.equals("wbmp")) {
            return "wbmp";
        } else {
            return DEFAULT_FORMAT;
        }
    }

    public static boolean isImage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String format = getFormat(filePath);
        return format.equals("jpg") || format.equals("png") || format.equals("gif") || format.equals("bmp") || format.equals("wbmp");
    }

    public static BufferedImage createThumbnail(BufferedImage image, int maxSize) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        double scale = (double) maxSize / Math.max(width, height);
        return scale(image, scale);
    }

    public static BufferedImage createThumbnail(BufferedImage image, int width, int height) {
        return scaleToFit(image, width, height);
    }

    public static BufferedImage cropSquare(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int size = Math.min(image.getWidth(), image.getHeight());
        return cropCenter(image, size, size);
    }

    public static BufferedImage cropCircle(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int size = Math.min(image.getWidth(), image.getHeight());
        BufferedImage square = cropCenter(image, size, size);

        BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = circleImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillOval(0, 0, size, size);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(square, 0, 0, null);
        g.dispose();
        return circleImage;
    }

    public static BufferedImage addBorder(BufferedImage image, int borderWidth, Color borderColor) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage borderedImage = new BufferedImage(width + 2 * borderWidth, height + 2 * borderWidth, image.getType());
        Graphics2D g = borderedImage.createGraphics();
        g.setColor(borderColor);
        g.fillRect(0, 0, width + 2 * borderWidth, height + 2 * borderWidth);
        g.drawImage(image, borderWidth, borderWidth, null);
        g.dispose();
        return borderedImage;
    }

    public static BufferedImage addRoundedBorder(BufferedImage image, int borderWidth, int cornerRadius, Color borderColor) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage borderedImage = new BufferedImage(width + 2 * borderWidth, height + 2 * borderWidth, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = borderedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(borderColor);
        g.fillRoundRect(0, 0, width + 2 * borderWidth, height + 2 * borderWidth, cornerRadius, cornerRadius);
        g.drawImage(image, borderWidth, borderWidth, null);
        g.dispose();
        return borderedImage;
    }
}
