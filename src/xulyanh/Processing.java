/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xulyanh;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author PhongNT
 */


// class chứa các hàm static để xử lý các thuật toán yêu cầu trong bài tập
public class Processing {

    // hàm mở file ảnh 
    public static BufferedImage getPicture() {
        // ảnh được load từ bộ nhớ lưu vào bi
        BufferedImage bi = null;

        // tạo cửa sổ chọn File
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh cần xử lý");
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getAbsolutePath().endsWith(".png") || f.getAbsolutePath().endsWith(".jpg")
                        || f.getAbsolutePath().endsWith(".PNG");
            }

            @Override
            public String getDescription() {
                return "All Picture Files ";
            }
        });
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                String s = chooser.getSelectedFile().getAbsolutePath();
                bi = ImageIO.read(new File(s));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // trả về giá trị ảnh vừa  load từ ổ cứng
        return bi;
    }

    // hàm chuyển đổi ảnh màu thành ảnh xám
    public static BufferedImage getGrayscaleImage(BufferedImage src) {

        BufferedImage gImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = src.getRaster();
        WritableRaster gr = gImg.getRaster();
        for (int i = 0; i < wr.getWidth(); i++) {
            for (int j = 0; j < wr.getHeight(); j++) {
                gr.setSample(i, j, 0, wr.getSample(i, j, 0));
            }
        }
        gImg.setData(gr);
        return gImg;
    }

    // hàm copy giá trị đối tượng ảnh
    public static BufferedImage deepCopyImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    // hàm tính tần xuất xuất hiện của các giá trị mức xám trong ảnh và lưu vào mảng histoo
    public static int[] histogram(BufferedImage src) {
        WritableRaster wr = src.getRaster();
        // vì ảnh xám có 256 mức nên sử dung mảng này 
        // còn ảnh màu có số mức khác nên ảnh màu ko áp dụng vào hàm chính xác được
        int[] histoo = new int[256];

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                histoo[wr.getSample(x, y, 0)]++;
            }
        }
        return histoo;
    }

    // hàm cân bằng mức xám của ảnh xám
    // hai giai đoạn : 
    // + Chuyển ảnh thành mức xám đã nếu ảnh đó là ảnh màu
    // + Cân bằng
    public static BufferedImage equalize(BufferedImage src) {
        // mảng lưu trữ tần số của các mức xám trong Histogram
        src = getGrayscaleImage(src);

        // tính histogram lưu vào mảng
        int[] histo = histogram(src);

        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster wr = src.getRaster();
        WritableRaster er = nImg.getRaster();

        int totpix = wr.getWidth() * wr.getHeight();

        int[] chistogram = new int[256];
        chistogram[0] = histo[0];
        for (int i = 1; i < 256; i++) {
            chistogram[i] = chistogram[i - 1] + histo[i];
        }

        float[] arr = new float[256];
        for (int i = 0; i < 256; i++) {
            arr[i] = (float) ((chistogram[i] * 255.0) / (float) totpix);
        }

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                int nVal = (int) arr[wr.getSample(x, y, 0)];
                er.setSample(x, y, 0, nVal);
            }
        }
        nImg.setData(er);
        return nImg;
    }

    public static BufferedImage negativeImage(BufferedImage src) throws IOException {
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color color = new Color(src.getRGB(i, j));
                Color newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
                nImg.setRGB(i, j, newColor.getRGB());
            }
        }
        return nImg;
    }

    public static BufferedImage thresholding(BufferedImage src, int nThreshold) throws IOException {
        // phải tạo ra 1 biến temp xử lý chứ không nó sẽ làm thay đổi ảnh truyền vào.
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color color = new Color(src.getRGB(i, j));
                int x = (color.getRed() > nThreshold) ? 255 : 0;
                int y = (color.getGreen() > nThreshold) ? 255 : 0;
                int z = (color.getBlue() > nThreshold) ? 255 : 0;
                Color newColor = new Color(x, y, z);
                nImg.setRGB(i, j, newColor.getRGB());
            }
        }
        return nImg;
    }

    public static BufferedImage logarithmicTransformation(BufferedImage src, int c) throws IOException {

        // phải tạo ra 1 biến temp xử lý chứ không nó sẽ làm thay đổi ảnh truyền vào.
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color color = new Color(src.getRGB(i, j));
                // vì các giá trị pixel là int nên phải ép kiểu về int từ biểu thứ math
                int red = c * (int) Math.log(color.getRed() + 1);
                int green = c * (int) Math.log(color.getGreen() + 1);
                int blue = c * (int) Math.log(color.getBlue() + 1);

                Color newColor = new Color(red, green, blue);
                nImg.setRGB(i, j, newColor.getRGB());
            }
        }
        return nImg;
    }

    public static BufferedImage powerLawTransforms(BufferedImage src, float gamma, int c) throws IOException {
        // phải tạo ra 1 biến temp xử lý chứ không nó sẽ làm thay đổi ảnh truyền vào.
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color color = new Color(src.getRGB(i, j));
                // vì các giá trị pixel là int nên phải ép kiểu về int từ biểu thứ math
                int red = (int) (Math.pow((color.getRed()), gamma) * c);
                int green = (int) (Math.pow((color.getGreen()), gamma) * c);
                int blue = (int) (Math.pow((color.getBlue()), gamma) * c);
                Color newColor = new Color(red, green, blue);
                nImg.setRGB(i, j, newColor.getRGB());
            }
        }
        return nImg;
    }

    public static BufferedImage bitPlaneSlicing(BufferedImage src, int constant) throws IOException {
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color color = new Color(src.getRGB(i, j));
                int red = color.getRed() | (int) (Math.pow(2, constant));
                int green = color.getGreen() | (int) (Math.pow(2, constant));
                int blue = color.getBlue() | (int) (Math.pow(2, constant));
                Color newColor = new Color(red, green, blue);
                nImg.setRGB(i, j, newColor.getRGB());
            }
        }
        return nImg;
    }

    public static BufferedImage minNeighbourhoodImage(BufferedImage src) throws IOException {

        Color[] color;
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        // bỏ dòng và cột đầu tiên của ma trận nên duyện từ 1 tới độ dài - 1
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int[] sum = new int[9];
                for (int u = 0; u < 9; u++) {
                    sum[u] = sumRbgPerPixel(color[u]);
                }
                Color newColor = color[minPixelValue(sum)];
                bi.setRGB(i, j, newColor.getRGB());
            }
        }

        return bi;
    }

    public static BufferedImage maxNeighbourhoodImage(BufferedImage src) throws IOException {

        Color[] color;
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        // bỏ dòng và cột đầu tiên của ma trận nên duyện từ 1 tới độ dài - 1
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int[] sum = new int[9];
                for (int u = 0; u < 9; u++) {
                    sum[u] = sumRbgPerPixel(color[u]);
                }
                Color newColor = color[maxPixelValue(sum)];
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage medianImage(BufferedImage src) throws IOException {

        Color[] color;
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // bỏ dòng và cột đầu tiên của ma trận nên duyện từ 1 tới độ dài - 1
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int[] sum = new int[9];
                for (int u = 0; u < 9; u++) {
                    sum[u] = sumRbgPerPixel(color[u]);
                }

                // sắp xếp mảng để lấy trung vị
                Arrays.sort(sum);
                Color newColor = new Color(sum[4] / 3, sum[4] / 3, sum[4] / 3);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage averageImage(BufferedImage src) throws IOException {
        Color[] color;
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int greenValue = 0;
                int blueValue = 0;
                for (int u = 0; u < 9; u++) {
                    redValue += color[u].getRed();
                    greenValue += color[u].getGreen();
                    blueValue += color  [u].getBlue();
                }
                Color newColor = new Color(redValue / 9, greenValue / 9, blueValue / 9);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage weightAverageImage(BufferedImage src) throws IOException {
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int greenValue = 0;
                int blueValue = 0;
                int heso;
                for (int u = 0; u < 9; u++) {
                    if (u == 4) { // Lấy vị trí trung tâm ma trận 3x3
                        heso = 4;
                    } else if (u % 2 == 0) { // Các vị trí 0 2 6 8
                        heso = 1;
                    } else { // Các vị trí 1 3 5 7
                        heso = 2;
                    }
                    redValue += color[u].getRed() * heso;
                    greenValue += color[u].getGreen() * heso;
                    blueValue += color[u].getBlue() * heso;
                }
                Color newColor = new Color(redValue / 16, greenValue / 16,
                        blueValue / 16);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    // xử lý viền đen , và trắng..
    public static BufferedImage padImage(BufferedImage src, int nEdge, int option) throws IOException {

        int mau;
        if (option == 1) {
            mau = 255; // màu trắng
        } else {
            mau = 0; // màu đen
        }
        int witdth = src.getWidth();
        int height = src.getHeight();

        BufferedImage bi = new BufferedImage(witdth, height, src.getType());
        for (int i = 0; i < witdth; i++) {

            for (int j = 0; j < height; j++) {

                if (i < nEdge / 2 || i > (witdth - nEdge / 2 - 1) || j < nEdge / 2 || j > (witdth - nEdge / 2 - 1)) {
                    Color newColor = new Color(mau, mau, mau);
                    bi.setRGB(i, j, newColor.getRGB());

                } else {

                    Color newColor1 = new Color(src.getRGB(i, j));
                    bi.setRGB(i, j, newColor1.getRGB());
                }

            }
        }
        return bi;
    }

    // xử lý viền bằng cách clone viền
    public static BufferedImage replicateBorder(BufferedImage src, int nEdge) throws IOException {
        int witdth = src.getWidth();
        int height = src.getHeight();
        BufferedImage bi = new BufferedImage(witdth, height, src.getType());
        for (int i = 0; i < witdth; i++) {

            for (int j = 0; j < height; j++) {

                if (i < nEdge / 2 || i > (witdth - nEdge / 2 - 1) || j < nEdge / 2 || j > (witdth - nEdge / 2 - 1)) {
                    Color newColor = new Color(src.getRGB(nEdge / 2 + 1, j));
                    bi.setRGB(i, j, newColor.getRGB());

                } else {

                    Color newColor1 = new Color(src.getRGB(i, j));
                    bi.setRGB(i, j, newColor1.getRGB());
                }

            }
        }
        return bi;
    }

    public static BufferedImage laplcianFilteredImage(BufferedImage src) throws IOException {
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int greenValue = 0;
                int blueValue = 0;
                int heso;
                for (int u = 0; u < 9; u++) {
                    if (u == 4) { // Lấy vị trí trung tâm ma trận 3x3
                        heso = -4;
                    } else if (u % 2 == 0) { // Các vị trí 0 2 6 8
                        heso = 1;
                    } else { // Các vị trí 1 3 5 7
                        heso = 0;
                    }
                    redValue += color[u].getRed() * heso;
                    greenValue += color[u].getGreen() * heso;
                    blueValue += color[u].getBlue() * heso;
                }
                if (redValue > 255) {
                    redValue = 255;
                } else if (redValue < 0) {
                    redValue = 0;
                }
                if (greenValue > 255) {
                    greenValue = 255;
                } else if (greenValue < 0) {
                    greenValue = 0;
                }
                if (blueValue > 255) {
                    blueValue = 255;
                } else if (blueValue < 0) {
                    blueValue = 0;
                }

                Color newColor = new Color(redValue, greenValue,
                        blueValue);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage sharpeneImage(BufferedImage src) throws IOException {
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int greenValue = 0;
                int blueValue = 0;
                int heso;
                for (int u = 0; u < 9; u++) {
                    if (u == 4) { // Lấy vị trí trung tâm ma trận 3x3
                        heso = 5;
                    } else if (u % 2 == 0) { // Các vị trí 0 2 6 8
                        heso = -1;
                    } else { // Các vị trí 1 3 5 7
                        heso = 0;
                    }
                    redValue += color[u].getRed() * heso;
                    greenValue += color[u].getGreen() * heso;
                    blueValue += color[u].getBlue() * heso;
                }
                if (redValue > 255) {
                    redValue = 255;
                } else if (redValue < 0) {
                    redValue = 0;
                }
                if (greenValue > 255) {
                    greenValue = 255;
                } else if (greenValue < 0) {
                    greenValue = 0;
                }
                if (blueValue > 255) {
                    blueValue = 255;
                } else if (blueValue < 0) {
                    blueValue = 0;
                }

                Color newColor = new Color(redValue, greenValue,
                        blueValue);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage sobelFilter(BufferedImage src) throws IOException {
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                double redValue = 0;
                int redValue1 = 0;
                int redValue2 = 0;
                double greenValue = 0;
                int greenValue1 = 0;
                int greenValue2 = 0;
                double blueValue = 0;
                int blueValue1 = 0;
                int blueValue2 = 0;

                redValue1 = (((-1 * color[0].getRed()) + (0 * color[1].getRed()) + (1 * color[2].getRed()))
                        + ((-2 * color[3].getRed()) + (0 * color[4].getRed()) + (2 * color[5].getRed()))
                        + ((-1 * color[6].getRed()) + (0 * color[7].getRed()) + (1 * color[8].getRed())));

                redValue2 = (((-1 * color[0].getRed()) + (-2 * color[1].getRed()) + (-1 * color[2].getRed()))
                        + ((0 * color[3].getRed()) + (0 * color[4].getRed()) + (0 * color[5].getRed()))
                        + ((1 * color[6].getRed()) + (2 * color[7].getRed()) + (1 * color[8].getRed())));

                greenValue1 = (((-1 * color[0].getGreen()) + (0 * color[1].getGreen()) + (1 * color[2].getGreen()))
                        + ((-2 * color[3].getGreen()) + (0 * color[4].getGreen()) + (2 * color[5].getGreen()))
                        + ((-1 * color[6].getGreen()) + (0 * color[7].getGreen()) + (1 * color[8].getGreen())));

                greenValue2 = (((-1 * color[0].getGreen()) + (-2 * color[1].getGreen()) + (-1 * color[2].getGreen()))
                        + ((0 * color[3].getGreen()) + (0 * color[4].getGreen()) + (0 * color[5].getGreen()))
                        + ((1 * color[6].getGreen()) + (2 * color[7].getGreen()) + (1 * color[8].getGreen())));

                blueValue1 = (((-1 * color[0].getBlue()) + (0 * color[1].getBlue()) + (1 * color[2].getBlue()))
                        + ((-2 * color[3].getBlue()) + (0 * color[4].getBlue()) + (2 * color[5].getBlue()))
                        + ((-1 * color[6].getBlue()) + (0 * color[7].getBlue()) + (1 * color[8].getBlue())));

                blueValue2 = (((-1 * color[0].getBlue()) + (-2 * color[1].getBlue()) + (-1 * color[2].getBlue()))
                        + ((0 * color[3].getBlue()) + (0 * color[4].getBlue()) + (0 * color[5].getBlue()))
                        + ((1 * color[6].getBlue()) + (2 * color[7].getBlue()) + (1 * color[8].getBlue())));

                redValue = Math.sqrt((redValue1 * redValue1) + (redValue2 * redValue2));
                greenValue = Math.sqrt((greenValue1 * greenValue1) + (greenValue2 * greenValue2));
                blueValue = Math.sqrt((blueValue1 * blueValue1) + (blueValue2 * blueValue2));

                if (redValue > 255) {
                    redValue = 255;
                } else if (redValue < 0) {
                    redValue = 0;
                }
                if (greenValue > 255) {
                    greenValue = 255;
                } else if (greenValue < 0) {
                    greenValue = 0;
                }
                if (blueValue > 255) {
                    blueValue = 255;
                } else if (blueValue < 0) {
                    blueValue = 0;
                }

                Color newColor = new Color((int) redValue, (int) greenValue,
                        (int) blueValue);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage sobelFilterWithThesholding(BufferedImage src) throws IOException {
        // hàm tính ngưỡng T
        int thresholding = basicGlobalThresholding(src);

        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                double redValue = 0;
                int redValue1 = 0;
                int redValue2 = 0;
                double greenValue = 0;
                int greenValue1 = 0;
                int greenValue2 = 0;
                double blueValue = 0;
                int blueValue1 = 0;
                int blueValue2 = 0;

                redValue1 = (((-1 * color[0].getRed()) + (0 * color[1].getRed()) + (1 * color[2].getRed()))
                        + ((-2 * color[3].getRed()) + (0 * color[4].getRed()) + (2 * color[5].getRed()))
                        + ((-1 * color[6].getRed()) + (0 * color[7].getRed()) + (1 * color[8].getRed())));

                redValue2 = (((-1 * color[0].getRed()) + (-2 * color[1].getRed()) + (-1 * color[2].getRed()))
                        + ((0 * color[3].getRed()) + (0 * color[4].getRed()) + (0 * color[5].getRed()))
                        + ((1 * color[6].getRed()) + (2 * color[7].getRed()) + (1 * color[8].getRed())));

                greenValue1 = (((-1 * color[0].getGreen()) + (0 * color[1].getGreen()) + (1 * color[2].getGreen()))
                        + ((-2 * color[3].getGreen()) + (0 * color[4].getGreen()) + (2 * color[5].getGreen()))
                        + ((-1 * color[6].getGreen()) + (0 * color[7].getGreen()) + (1 * color[8].getGreen())));

                greenValue2 = (((-1 * color[0].getGreen()) + (-2 * color[1].getGreen()) + (-1 * color[2].getGreen()))
                        + ((0 * color[3].getGreen()) + (0 * color[4].getGreen()) + (0 * color[5].getGreen()))
                        + ((1 * color[6].getGreen()) + (2 * color[7].getGreen()) + (1 * color[8].getGreen())));

                blueValue1 = (((-1 * color[0].getBlue()) + (0 * color[1].getBlue()) + (1 * color[2].getBlue()))
                        + ((-2 * color[3].getBlue()) + (0 * color[4].getBlue()) + (2 * color[5].getBlue()))
                        + ((-1 * color[6].getBlue()) + (0 * color[7].getBlue()) + (1 * color[8].getBlue())));

                blueValue2 = (((-1 * color[0].getBlue()) + (-2 * color[1].getBlue()) + (-1 * color[2].getBlue()))
                        + ((0 * color[3].getBlue()) + (0 * color[4].getBlue()) + (0 * color[5].getBlue()))
                        + ((1 * color[6].getBlue()) + (2 * color[7].getBlue()) + (1 * color[8].getBlue())));

                redValue = Math.sqrt((redValue1 * redValue1) + (redValue2 * redValue2));
                greenValue = Math.sqrt((greenValue1 * greenValue1) + (greenValue2 * greenValue2));
                blueValue = Math.sqrt((blueValue1 * blueValue1) + (blueValue2 * blueValue2));

//                if (redValue > 255) {
//                    redValue = 255;
//                } else if (redValue < 0) {
//                    redValue = 0;
//                }
//                if (greenValue > 255) {
//                    greenValue = 255;
//                } else if (greenValue < 0) {
//                    greenValue = 0;
//                }
//                if (blueValue > 255) {
//                    blueValue = 255;
//                } else if (blueValue < 0) {
//                    blueValue = 0;
//                }
                Color newColor;
                if ((int) redValue >= thresholding) {
                    newColor = new Color(255, 255, 255);
                } else {
                    newColor = new Color(0, 0, 0);
                }
//                Color newColor = new Color((int) redValue, (int) greenValue,
//                        (int) blueValue);
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    private static int sumRbgPerPixel(Color color) {
        return color.getRed() + color.getGreen() + color.getBlue();
    }

    private static int minPixelValue(int[] sum) {
        int position = 0;

        // >1000 mới đúng...
        int min = 1000;
        for (int i = 0; i < sum.length; i++) {
            if (sum[i] < min) {
                position = i;
                min = sum[i];
            }
        }
        return position;
    }

    private static int maxPixelValue(int[] sum) {
        int position = 0;
        int max = 0;
        for (int i = 0; i < sum.length; i++) {
            if (sum[i] > max) {
                position = i;
                max = sum[i];
            }
        }
        return position;
    }

    // sử dụng bộ loc (filters 3x3)  => 9 giá trị neighbourhood
    public static Color[] getFilterValues(BufferedImage src, int i, int j) {
        Color[] color = new Color[9];
        color[0] = new Color(src.getRGB(i - 1, j - 1));
        color[1] = new Color(src.getRGB(i, j - 1));
        color[2] = new Color(src.getRGB(i + 1, j - 1));
        color[3] = new Color(src.getRGB(i - 1, j));
        color[4] = new Color(src.getRGB(i, j));
        color[5] = new Color(src.getRGB(i + 1, j));
        color[6] = new Color(src.getRGB(i - 1, j + 1));
        color[7] = new Color(src.getRGB(i, j + 1));
        color[8] = new Color(src.getRGB(i + 1, j + 1));
        return color;
    }

    public static int basicGlobalThresholding(BufferedImage src) {
        long sumGrey = 0;
        // ngưỡng T
        int T, T1;
        Color c;
        for (int i = 1; i < src.getWidth(); i++) {
            for (int j = 1; j < src.getHeight(); j++) {
                c = new Color(src.getRGB(i, j));
                sumGrey += (long) c.getGreen();

            }
        }

        // giá trị khởi tạo ban đầu của ngưỡng T
        T = (int) sumGrey / (src.getHeight() * src.getWidth());

        int sumGrey1, sumGrey2, dem1, dem2;
        // vòng lặp tìm T
        do {
            sumGrey1 = 0;
            sumGrey2 = 0;
            dem1 = 0;
            dem2 = 0;
            for (int i = 0; i < src.getWidth(); i++) {
                for (int j = 0; j < src.getHeight(); j++) {
                    c = new Color(src.getRGB(i, j));
                    int greyValue = c.getGreen();

                    if (greyValue <= T) {
                        sumGrey1 += greyValue;
                        dem1++;
                    } else {
                        sumGrey2 += greyValue;
                        dem2++;
                    }
                }
            }

            // xử lý khi giá trị dem = 0; nếu  = 0 gán cho giá trị 1
            // để khởi bị lỗi ngoại lện chia cho 0.
            if (dem1 == 0) {
                dem1 = 1;
            }
            if (dem2 == 0) {
                dem2 = 1;
            }
            T1 = (sumGrey1 / dem1 + sumGrey2 / dem2) / 2;

            // dâu hiệu kêt thúc vòng lặp vô tận
            if ((int) T1 == (int) T) {
                break;
            } else {

                // nếu không thì cập nhật lại giá trị T
                T = T1;
            }

        } while (true);
        return T;
    }

    public static BufferedImage pointDectection(BufferedImage src) throws IOException {
        // hàm tính ngưỡng T
        int thresholding = basicGlobalThresholding(src);
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int heso;
                for (int u = 0; u < 9; u++) {
                    if (u == 4) { // Lấy vị trí trung tâm ma trận 3x3
                        heso = 8;
                    } else { // Các vị trí còn lại 
                        heso = -1;
                    }
                    redValue += color[u].getRed() * heso;

                }
                Color newColor;
                if (redValue > thresholding) {
                    newColor = new Color(255, 255, 255);
                } else {
                    newColor = new Color(0, 0, 0);
                }

                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    // chuyển ảnh về nhị phân trắng đen chỉ có giá trị mức xám là 0, 255
    public static BufferedImage getBlackWhiteImage(BufferedImage src) throws IOException {
        // hàm tính ngưỡng T
        int thresholding = basicGlobalThresholding(src);
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color newColor;
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {

                newColor = new Color(src.getRGB(i, j));
                if (newColor.getGreen() >= thresholding) {
                    newColor = new Color(255, 255, 255);

                } else {
                    newColor = new Color(0, 0, 0);

                }
                bi.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi;
    }

    public static BufferedImage linesDetection(BufferedImage src, int flag) throws IOException {
        int thresholding = basicGlobalThresholding(src);
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color[] color;
        Color newColor;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(src, i, j);
                int redValue = 0;
                int heso;
                switch (flag) {
                    case 1:
                        for (int u = 0; u < 9; u++) {
                            if (u == 3 || u == 4 || u == 5) {
                                heso = 2;
                            } else {
                                heso = -1;
                            }
                            redValue += color[u].getRed() * heso;
                        }
                        break;
                    case 2:
                        for (int u = 0; u < 9; u++) {
                            if (u == 2 || u == 4 || u == 6) {
                                heso = 2;
                            } else {
                                heso = -1;
                            }
                            redValue += color[u].getRed() * heso;
                        }
                        break;
                    case 3:
                        for (int u = 0; u < 9; u++) {
                            if (u == 1 || u == 4 || u == 7) {
                                heso = 2;
                            } else {
                                heso = -1;
                            }
                            redValue += color[u].getRed() * heso;
                        }
                        break;
                    case 4:
                        for (int u = 0; u < 9; u++) {
                            if (u == 0 || u == 4 || u == 8) {
                                heso = 2;
                            } else {
                                heso = -1;
                            }
                            redValue += color[u].getRed() * heso;
                        }
                        break;

                    default:
                }

                if (redValue > thresholding) {
                    newColor = new Color(255, 255, 255);
                } else {
                    newColor = new Color(0, 0, 0);
                }
                // set giá trị vào pixel
                bi.setRGB(i, j, newColor.getRGB());
            }

        }
        return bi;
    }

    // chỉ áp dụng trên ảnh nhị phân trắng đen (0 và 255), đối tượng là màu trắng còn nền là màu đen
    public static BufferedImage erosion(BufferedImage src) throws IOException {
        // chuyển ảnh về nhị phân trắng đen chỉ có giá trị mức xám là 0, 255
        BufferedImage bi = getBlackWhiteImage(src);
        BufferedImage bi1 = new BufferedImage(src.getWidth(), src.getHeight(), bi.getType());
        Color[] color;
        Color newColor;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(bi, i, j);

                if (color[1].getGreen() == 255 && color[3].getGreen() == 255
                        && color[5].getGreen() == 255 && color[4].getGreen() == 255
                        && color[7].getGreen() == 255) {
                    // xảy ra trường hợp fit, gán giá trị 255
                    newColor = new Color(255, 255, 255);
                } else {

                    // không xảy ra fit, gán giá trị 0
                    newColor = new Color(0, 0, 0);

                }
                // áp dụng cho đối tượng là màu đen, còn nền là màu trắnng

//                 if (color[1].getGreen() == 0 && color[3].getGreen() == 0 && color[5].getGreen() == 0
//                        && color[4].getGreen() == 0 && color[7].getGreen() == 0) {
//                    // xảy ra trường hợp fit, gán giá trị 255
//                    newColor = new Color(0, 0, 0);
//                } else {
//
//                    // không xảy ra fit, gán giá trị 0
//                    newColor = new Color(255, 255, 255);
//
//                }
                bi1.setRGB(i, j, newColor.getRGB());
            }
        }

        return bi1;
    }

    // chỉ áp dụng trên ảnh nhị phân trắng đen (0 và 255), đối tượng là màu trắng còn nền là màu đen
    public static BufferedImage dilation(BufferedImage src) throws IOException {
        // chuyển ảnh về nhị phân trắng đen chỉ có giá trị mức xám là 0, 255
        BufferedImage bi = getBlackWhiteImage(src);
        BufferedImage bi1 = new BufferedImage(src.getWidth(), src.getHeight(), bi.getType());
        Color[] color;
        Color newColor;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {
                color = getFilterValues(bi, i, j);

                if (color[1].getGreen() == 255 || color[3].getGreen() == 255 || color[5].getGreen() == 255
                        || color[4].getGreen() == 255 || color[7].getGreen() == 255) {
                    // xảy ra trường hợp fit, gán giá trị 255
                    newColor = new Color(255, 255, 255);
                } else {

                    // không xảy ra fit, gán giá trị 0
                    newColor = new Color(0, 0, 0);

                }
                // áp dụng cho đối tượng là màu đen còn nền là màu trắng

//                 if (color[1].getGreen() == 0 || color[3].getGreen() == 0 || color[5].getGreen() == 0
//                        || color[4].getGreen() == 0 || color[7].getGreen() == 0) {
//                    // xảy ra trường hợp fit, gán giá trị 255
//                    newColor = new Color(0, 0, 0);
//                } else {
//
//                    // không xảy ra fit, gán giá trị 0
//                    newColor = new Color(255, 255, 255);
//
//                }
                bi1.setRGB(i, j, newColor.getRGB());
            }
        }
        return bi1;
    }

    // chỉ áp dụng trên ảnh nhị phân trắng đen (0 và 255), đối tượng là màu trắng còn nền là màu đen
    public static BufferedImage opening(BufferedImage src) throws IOException {
        BufferedImage bi = erosion(src);
        bi = dilation(bi);
        return bi;
    }

    // chỉ áp dụng trên ảnh nhị phân trắng đen (0 và 255), đối tượng là màu trắng còn nền là màu đen
    public static BufferedImage closing(BufferedImage src) throws IOException {
        BufferedImage bi = dilation(src);
        bi = erosion(bi);
        return bi;
    }

    // chỉ áp dụng trên ảnh nhị phân trắng đen (0 và 255), đối tượng là màu trắng còn nền là màu đen
    public static BufferedImage boundaryExtraction(BufferedImage src) throws IOException {
        BufferedImage bi2 = erosion(src);
        BufferedImage bi1 = getBlackWhiteImage(src);

        // ảnh sau khi xử lý sẽ lưu vào bi
        BufferedImage bi = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Color newColor, newColor1, newColor2;
        for (int i = 1; i < src.getWidth() - 1; i++) {
            for (int j = 1; j < src.getHeight() - 1; j++) {

                // giá trị màu của A tại vị trí i, j trong slide
                newColor1 = new Color(bi1.getRGB(i, j));

                // giá tri màu tại vị trí i, j sau khi  thực hiện co
                newColor2 = new Color(bi2.getRGB(i, j));

                //  gái trị màu tại vị trí i, j sau khi trừ.
                newColor = new Color(newColor1.getRed() - newColor2.getRed(), newColor1.getGreen() - newColor2.getGreen(),
                        newColor1.getBlue() - newColor2.getBlue());

                //set giá trị vào trong xử lý ảnh
                bi.setRGB(i, j, newColor.getRGB());
            }

        }
        return bi;
    }

}
