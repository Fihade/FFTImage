import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class FourierTransformer  extends JFrame{
    Image im;
    BufferedImage imageAuth = null;
    int iw;
    int ih;
    int[] pixels;
    int[] newPixels;
    File old = new File("C:/Users/swaggymiller/Desktop/2.jpg");
    File neew = new File("C:/Users/swaggymiller/Desktop/2-test.jpg");
    public FourierTransformer() {
        String zipImage = zipImageFile(old,neew,200,100,0.7f);
        try {
            this.im = ImageIO.read(new File(zipImage));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        this.iw = im.getWidth(null);
        this.ih = im.getHeight(null);
        pixels = new int[iw * ih];
        try {
            PixelGrabber pg = new PixelGrabber(im, 0, 0, iw, ih, pixels, 0, iw);
            pg.grabPixels();
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }

    }

    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(this.im, 0, 100, this.iw, this.ih, this);
        if(imageAuth != null)
            g.drawImage(imageAuth, 250, 100, imageAuth.getWidth(), imageAuth.getHeight(), this);

    }

    public static void main(String[] args){
        FourierTransformer frame = new FourierTransformer();

        frame.setSize(600, 500);
        frame.setTitle("ImageMenu");
        frame.setName("hello");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        frame.setVisible(true);
        frame.convert(frame.getGraphics());
    }
    public Image convert(Graphics g) {
        // 赋初值
        int w = 1;
        int h = 1;
        // 计算进行付立叶变换的宽度和高度（2的整数次方）
        while (w * 2 <= iw) {
            w *= 2;
        }
        while (h * 2 <= ih) {
            h *= 2;
        }
        // 分配内存
        Complex[] src = new Complex[h * w];
        Complex[] dest = new Complex[h * w];
        newPixels = new int[h * w];
        // 初始化newPixels
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                newPixels[i * w + j] = pixels[i * iw + j] & 0xff;
            }
        }
        // 初始化src,dest
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                dest[i * w + j] = new Complex();
                src[i * w + j] = new Complex(newPixels[i * w + j], 0);
            }
        }
        // 在y方向上进行快速傅立叶变换
        for (int i = 0; i < h; i++) {
            FFT.fft(src, i, w, dest);
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                src[j * h + i] = dest[i * w + j];
//              System.out.println("dest " + j*h+i + ",  src " + i*w+j);
            }
        }
        // 对x方向进行傅立叶变换
        for (int i = 0; i < w; i++) {
            FFT.fft(src, i, h, dest);
        }
        /**
         * 将图像看做二维函数，图像灰度值为函数在相应XY处的函数值，对其进行二维快速傅里叶变换，
         * 得到一个复数矩阵，将此矩阵水平循环移动半宽，垂直循环移动半高。
         */
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double re = dest[j * h + i].re;
                double im = dest[j * h + i].im;
                int ii = 0, jj = 0;
                int temp = (int) (Math.sqrt(re * re + im * im) / 100);
                if (temp > 255) {
                    temp = 255;
                }
                if (i < h / 2) {
                    ii = i + h / 2;
                } else {
                    ii = i - h / 2;
                }
                if (j < w / 2) {
                    jj = j + w / 2;
                } else {
                    jj = j - w / 2;
                }
                newPixels[ii * w + jj] = temp;
            }
        }

        imageAuth = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        ColorModel colorModel = imageAuth.getColorModel();
        WritableRaster raster = colorModel.createCompatibleWritableRaster(w, h);
        raster.setPixels(0, 0, w, h, newPixels);
        imageAuth.setData(raster);

        try {
            ImageIO.write(imageAuth, "png", new File("fftimage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.update(g);


        return imageAuth;
    }
    /**
     * 根据设置的宽高等比例压缩图片文件<br> 先保存原文件，再压缩、上传
     * @param oldFile  要进行压缩的文件
     * @param newFile  新文件
     * @param width  宽度 //设置宽度时（高度传入0，等比例缩放）
     * @param height 高度 //设置高度时（宽度传入0，等比例缩放）
     * @param quality 质量
     * @return 返回压缩后的文件的全路径
     */
    public static String zipImageFile(File oldFile,File newFile, int width, int height,float quality) {
        if (oldFile == null) {
            return null;
        }
        try {
            /** 对服务器上的临时文件进行处理 */
            Image srcFile = ImageIO.read(oldFile);
            int w = srcFile.getWidth(null);
            int h = srcFile.getHeight(null);
            double bili;
            if(width>0){
                bili=width/(double)w;
                height = (int) (h*bili);
            }else{
                if(height>0){
                    bili=height/(double)h;
                    width = (int) (w*bili);
                }
            }

            String srcImgPath = newFile.getAbsoluteFile().toString();
            System.out.println(srcImgPath);
            String subfix = "jpg";
            subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".")+1,srcImgPath.length());

            BufferedImage buffImg = null;
            if(subfix.equals("png")){
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, new File(srcImgPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile.getAbsolutePath();
    }
}
