# 使用前
如果无法运行，建议重新导入入OpneCV SDK，本人用的版本是opencv-4.5.4-android-sdk
# 核心代码
在ImageMethod类中
```java
    public  double detectVariousColor(Bitmap bmp, int colorMin, int colorMax) 

```
两个数组用于划分颜色,可根据检测物质来调整数组内容
```java
    public static double[][] HSV_VALUE_HIGH = {
            new double[]{10.0d, 255.0d, 255.0d},
            new double[]{25.0d, 255.0d, 255.0d},
            new double[]{34.0d, 255.0d, 255.0d},
            new double[]{77.0d, 255.0d, 255.0d},
            new double[]{99.0d, 255.0d, 255.0d},
            new double[]{124.0d, 255.0d, 255.0d},
            new double[]{155.0d, 255.0d, 255.0d},
            new double[]{180.0d, 255.0d, 46.0d},
            new double[]{180.0d, 43.0d, 220.0d},
            new double[]{180.0d, 30.0d, 255.0d},
            new double[]{180.0d, 255.0d, 255.0d}};
    public static double[][] HSV_VALUE_LOW = {
            new double[]{0.0d, 43, 46},
            new double[]{11.0d, 43, 46},
            new double[]{26.0d, 43, 46},
            new double[]{35.0d, 43, 46},
            new double[]{78.0d, 43, 46},
            new double[]{100.0d,43, 46},
            new double[]{125.0d, 43, 46},//6
            new double[]{0.0d, 0.0d, 0.0d},
            new double[]{0.0d, 0.0d, 46.0d},
            new double[]{0.0d, 0.0d, 221.0d},
            new double[]{156.0d, 43d, 0d}};
```
检测结果是RGB空间的欧式距离～
# 简介
比色法是一种分析物质浓度的方法，利用溶液的颜色强度与溶液中物质浓度成正比的关系进行测量。在化学实验室中，常常需要用到比色法来测量样品中某种物质的浓度，这种测量方法需要高精度的光学仪器和精密的数据处理程序，这使得比色法在实验室中的应用非常普遍。
为了实现基于智能手机的比色法检测系统，本章开发了一款基于Android平台的比色App。该App操作简便，如图所示。
使用该App时，用户只需点击“相册”或“拍照”按钮，即可将待测样品照片显示在软件的上半部分。接着，用户可以通过调整下方的“颜色最小值”和“颜色最大值”滑动条，轻松利用颜色来调整检测区域。此外，用户还可以点击“filter”按钮，选择高斯双边滤波，并通过调整滑动条获取检测区域。如果用户需要在低光强或低饱和度下获取颜色区域，则可点击“S0”或“V0”按钮进行选择。通过以上操作，用户可以轻松获得理想的测量区域轮廓，该轮廓会以红色线条显示。
最后，用户只需点击“比色”按钮，稍等片刻即可在文本框中查看所有区域的检测平均结果，并在每个轮廓中央和图片左上角看到该轮廓内的ΔE值。该App可根据用户的实验需要自由记录所需的检测结果，为实验工作提供更加便利的支持。
![VyzoW8.png](https://i.imgloc.com/2023/05/20/VyzoW8.png)
