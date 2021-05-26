## Android OpenGL ES渲染YUV数据

通过CameraX拿到原始YUV数据，然后交给OpenGL进行渲染。

代码中`separateYUV`函数是按照stride为2来实现的，实际需要根据`planes[1].getPixelStride()`的值来确认，如果`planes[1].getPixelStride()`为1，则`planes[1].getBuffer()`即为`U`分量，`planes[2].getBuffer()`即为`V`分量。

```
private void separateYUV(Image image) {
            int width = image.getWidth();
            int height = image.getHeight();

            Image.Plane[] planes = image.getPlanes();
            // planes[0]必定是完整Y分量
            ByteBuffer buffer1 = planes[0].getBuffer();
            // planes[1]和planes[2]分别为U和V分量
            ByteBuffer buffer2 = planes[1].getBuffer();
            ByteBuffer buffer3 = planes[2].getBuffer();

            int ySize = buffer1.remaining();
            int uSize = buffer2.remaining();

            // uv分量是y分量的1/4
            byte[] uBytes = new byte[ySize / 4];
            byte[] vBytes = new byte[ySize / 4];

            // U和V分量应该为Y分量的1/4，YUV_420_888中Stride为2，造成uSize为ySize的一半
            // 具体参考 https://www.polarxiong.com/archives/Android-Image%E7%B1%BB%E6%B5%85%E6%9E%90-%E7%BB%93%E5%90%88YUV_420_888.html
            for (int i = 0, j = 0; i < uSize; i++) {
                byte u = buffer2.get();
                byte v = buffer3.get();

                if (i % 2 == 0) {
                    uBytes[j] = u;
                    vBytes[j] = v;
                    j++;
                }
            }

            ByteBuffer uByteBuffer = ByteBuffer.wrap(uBytes);
            ByteBuffer vByteBuffer = ByteBuffer.wrap(vBytes);

            MainActivity.this.render.setData(width, height, buffer1, uByteBuffer, vByteBuffer);

            image.close();
        }
```

Tips: 代码没有获取摄像头权限，需要手动开启相机权限

### 参考：

Android OpenGLES绘制yuv420纹理：[>>>点我进入](https://cloud.tencent.com/developer/article/1333374)