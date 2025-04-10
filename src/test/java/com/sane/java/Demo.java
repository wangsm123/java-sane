package com.sane.java;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * author wangsm
 * Date 2025/4/9
 */
public class Demo {
    public static void main(String[] args) throws IOException {
        SaneInterface sane = SaneInterface.INSTANCE;
        sane.sane_init(null,null);
        PointerByReference deviceList = new PointerByReference();
        int status = sane.sane_get_devices(deviceList, SaneInterface.SANE_TRUE);
        if (status != SaneInterface.SANE_STATUS_GOOD) {
            System.out.println("获取设备列表失败："+sane.sane_strstatus(status));
            System.exit(-1);
        }
        Pointer returned = deviceList.getValue();
        Pointer[] array = returned.getPointerArray(0);
        if(array.length == 0){
            System.out.println("未查询到连接设备");
        }
        String name= "";
        for (Pointer p : array) {
            SaneInterface.SANE_Device dev = new SaneInterface.SANE_Device(p);
            System.out.println("设备型号："+dev.model+",设备名称："+dev.name);
            name=dev.name;
        }
        PointerByReference handleRef = new PointerByReference();
        status = sane.sane_open(name,handleRef);
        if (status != SaneInterface.SANE_STATUS_GOOD) {
            System.out.println("打开设备失败："+sane.sane_strstatus(status));
            System.exit(-1);
        }
        int count=0;
        while(true){
            status = sane.sane_start(handleRef.getValue());
            if (status != SaneInterface.SANE_STATUS_GOOD) {
                //有的扫描仪在这里返回结束标志，有的是read中返回
                if (count!=0 && status == SaneInterface.SANE_STATUS_NO_DOCS) {
                    break; // 扫描完成
                }
                sane.sane_close(handleRef.getValue());
                System.out.println("开启扫描失败："+sane.sane_strstatus(status));
                System.exit(-1);
            }
            SaneInterface.SaneParameters params = new SaneInterface.SaneParameters();
            status = sane.sane_get_parameters(handleRef.getValue(), params);
            if (status != SaneInterface.SANE_STATUS_GOOD) {
                System.out.println("获取图像参数失败："+sane.sane_strstatus(status));
                System.exit(-1);
            }
            int bytesPerPixel = params.format == SaneInterface.SANE_FRAME_GRAY ? params.depth : 24;
            // 根据模式创建 BufferedImage
            BufferedImage image;
            switch (bytesPerPixel) {
                case 1:
                    image = new BufferedImage(params.pixels_per_line, params.lines, BufferedImage.TYPE_BYTE_BINARY);
                    break;
                case 8:
                    image = new BufferedImage(params.pixels_per_line, params.lines, BufferedImage.TYPE_BYTE_GRAY);
                    break;
                case 24:
                    image = new BufferedImage(params.pixels_per_line, params.lines, BufferedImage.TYPE_INT_RGB);
                    break;
                default:
                    image=null;
                    break;
            }
            if(image==null){
                System.out.println("初始化image失败");
                System.exit(-1);
            }
            // 读取扫描数据并写入 BufferedImage
            byte[] buffer = new byte[params.bytes_per_line];
            IntByReference length = new IntByReference();
            for (int y = 0; y < params.lines; y++) {
                status = sane.sane_read(handleRef.getValue(), buffer, buffer.length, length);
                if (status != SaneInterface.SANE_STATUS_GOOD) {
                    if (count!=0 && status == SaneInterface.SANE_STATUS_NO_DOCS) {
                        break; // 扫描完成
                    }
                    System.out.println("读取图像信息失败:"+sane.sane_strstatus(status));
                    System.exit(-1);
                }

                // 根据模式处理像素数据
                switch (bytesPerPixel) {
                    case 1:
                        for (int x = 0; x < params.pixels_per_line; x++) {
                            int bitIndex = x % 8;
                            int byteIndex = x / 8;
                            int bitValue = (buffer[byteIndex] >> (7 - bitIndex)) & 0x01;
                            int pixelValue = bitValue == 0 ? 255 : 0;
                            image.setRGB(x, y, pixelValue << 16 | pixelValue << 8 | pixelValue);
                        }
                        break;
                    case 8:
                        for (int x = 0; x < params.pixels_per_line; x++) {
                            int grayValue = buffer[x] & 0xFF;
                            image.getRaster().setSample(x, y, 0, grayValue);
                        }
                        break;
                    case 24:
                        for (int x = 0; x < params.pixels_per_line; x++) {
                            int r = buffer[x * 3] & 0xFF;
                            int g = buffer[x * 3 + 1] & 0xFF;
                            int b = buffer[x * 3 + 2] & 0xFF;
                            int rgb = (r << 16) | (g << 8) | b;
                            image.setRGB(x, y, rgb);
                        }
                        break;
                    default:
                        break;
                }
            }
            ImageIO.write(image,"jpeg",new File(count+".jpg"));
            System.out.println("生成第"+count+"张图像成功");
            count++;
        }
        sane.sane_cancel(handleRef.getValue());
        sane.sane_close(handleRef.getValue());
        sane.sane_exit();
    }
}
