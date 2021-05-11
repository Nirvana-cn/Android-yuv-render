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

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            MainActivity.this.render.setData(image.getWidth(), image.getHeight(), yBuffer, uBuffer, vBuffer);


        }

        private byte[] getYUV420FromImage(Image image) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] yuv420 = new byte[ySize * 3 / 2];
            yBuffer.get(yuv420, 0, ySize);

            int stride1 = planes[1].getPixelStride();
            int stride2 = planes[2].getPixelStride();
            if (stride1 == 1 && stride2 == 1) {
                // YUV 420
                uBuffer.get(yuv420, ySize, uSize);
                vBuffer.get(yuv420, ySize + uSize, vSize);
            } else if (stride1 == 2 && stride2 == 2) {
                // YUV 422
                int start = (int) ySize * 5 / 4;
                for (int i = 0, j = 0; i < uSize; i++) {
                    if (i % 2 == 0) {
                        yuv420[ySize + j] = uBuffer.get(i);
                        yuv420[start + j] = vBuffer.get(i);
                        j++;
                    }
                }
            }

            return yuv420;
        }
    }

}