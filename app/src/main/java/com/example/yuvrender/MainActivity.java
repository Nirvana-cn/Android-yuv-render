package com.example.yuvrender;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.LifecycleOwner;

import android.media.Image;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Rational;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    private Render render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        render = new Render();
        glSurfaceView.setRenderer(render);
        setContentView(glSurfaceView);

        openCamera(this);
    }


    private void openCamera(LifecycleOwner mLifecycleOwner) {
        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetAspectRatio(new Rational(1, 1))
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer(new PhotoAnalyzer());

        CameraX.bindToLifecycle(mLifecycleOwner, imageAnalysis);
    }

    private class PhotoAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy imageProxy, int rotationDegrees) {
            final Image image = imageProxy.getImage();
            if (image == null) {
                return;
            }

            separateYUV(image);
        }

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
    }

}