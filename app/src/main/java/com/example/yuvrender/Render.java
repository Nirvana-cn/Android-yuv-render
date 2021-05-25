package com.example.yuvrender;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.yuvrender.utils.ProgramHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class Render implements GLSurfaceView.Renderer {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;
    private static final String A_POSITION = "a_Position";
    private static final String A_TexturePosition = "a_TexturePosition";
    private static final String U_Y = "u_Y";
    private static final String U_U = "u_U";
    private static final String U_V = "u_V";

    private static final String vertexShader = "" +
            "attribute vec4 a_Position;\n" +
            "attribute vec2 a_TexturePosition;\n" +
            "varying vec2 v_TexturePosition;\n" +
            "void main()\n" +
            "{\n" +
            "    v_TexturePosition = a_TexturePosition;\n" +
            "    gl_Position = a_Position;\n" +
            "}";

    private static final String fragmentShader = "" +
            "precision mediump float;\n" +
            "varying vec2 v_TexturePosition;\n" +
            "uniform sampler2D u_Y;\n" +
            "uniform sampler2D u_U;\n" +
            "uniform sampler2D u_V;\n" +
            "void main()\n" +
            "{\n" +
            "    float y,u,v;\n" +
            "    y = texture2D(u_Y, v_TexturePosition).r;\n" +
            "    u = texture2D(u_U, v_TexturePosition).r - 0.5;\n" +
            "    v = texture2D(u_V, v_TexturePosition).r - 0.5;\n" +
            "    vec3 rgb;\n" +
            "    rgb.r = y + 1.403 * v;\n" +
            "    rgb.g = y - 0.344 * u - 0.714 * v;\n" +
            "    rgb.b = y + 1.770 * u;\n" +
            "    gl_FragColor = vec4(rgb, 1.0);\n" +
            "}";


    private FloatBuffer vertexData;
    private FloatBuffer textureData;
    private int program;
    private int aPositionLocation;
    private int aTexturePositionLocation;
    private int samplerY;
    private int samplerU;
    private int samplerV;
    private int[] textureIdYUV;

    private int width_yuv;
    private int height_yuv;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    public Render() {
        float[] vertices = {
                // 三角形带
                -1f, -1f, 0.0f,
                1f, -1f, 0.0f,
                -1f, 1f, 0.0f,
                1f, 1f, 0.0f,
        };

        float[] textureVertices = {
                // 三角形带
                0f, 1f, 0.0f,
                1f, 1f, 0.0f,
                0f, 0f, 0.0f,
                1f, 0f, 0.0f,
        };

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices);

        textureData = ByteBuffer
                .allocateDirect(textureVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        program = ProgramHelper.makeProgram(vertexShader, fragmentShader);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        aTexturePositionLocation = glGetAttribLocation(program, A_TexturePosition);
        textureData.position(0);
        glVertexAttribPointer(aTexturePositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, textureData);
        glEnableVertexAttribArray(aTexturePositionLocation);

        samplerY = GLES20.glGetUniformLocation(program, U_Y);
        samplerU = GLES20.glGetUniformLocation(program, U_U);
        samplerV = GLES20.glGetUniformLocation(program, U_V);

        //创建3个纹理
        textureIdYUV = new int[3];
        GLES20.glGenTextures(3, textureIdYUV, 0);
        //绑定纹理
        for (int id : textureIdYUV) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
            //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        if (width_yuv > 0 && height_yuv > 0) {
            //激活纹理0来绑定y数据
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdYUV[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv, height_yuv, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);

            //激活纹理1来绑定u数据
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdYUV[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv / 2, height_yuv / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);

            //激活纹理2来绑定v数据
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdYUV[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv / 2, height_yuv / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);

            GLES20.glUniform1i(samplerY, 0);
            GLES20.glUniform1i(samplerU, 1);
            GLES20.glUniform1i(samplerV, 2);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }
    }

    public void setData(int w, int h, ByteBuffer y, ByteBuffer u, ByteBuffer v) {
        this.width_yuv = w;
        this.height_yuv = h;
        this.y = y;
        this.u = u;
        this.v = v;
    }
}
