package com.example.yuvrender.utils;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";
    /**
     * 编译顶点着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param type       着色器类型
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    private static int compileShader(int type, String shaderCode) {
        // 1.创建一个新的着色器对象
        final int shaderObjectId = GLES20.glCreateShader(type);

        // 2.获取创建状态
        if (shaderObjectId == 0) {
            // 在OpenGL中，都是通过整型值去作为OpenGL对象的引用。之后进行操作的时候都是将这个整型值传回给OpenGL进行操作。
            // 返回值0代表着创建对象失败。
            Log.w(TAG, "Could not create new shader.");
            return 0;
        }

        // 3.将着色器代码上传到着色器对象中
        GLES20.glShaderSource(shaderObjectId, shaderCode);

        // 4.编译着色器对象
        GLES20.glCompileShader(shaderObjectId);

        // 5.获取编译状态：OpenGL将想要获取的值放入长度为1的数组的首位
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + GLES20.glGetShaderInfoLog(shaderObjectId));

        // 6.验证编译状态
        if (compileStatus[0] == 0) {
            // 如果编译失败，则删除创建的着色器对象
            GLES20.glDeleteShader(shaderObjectId);

            Log.w(TAG, "Compilation of shader failed.");

            // 7.返回着色器对象：失败，为0
            return 0;
        }

        // 7.返回着色器对象：成功，非0
        return shaderObjectId;
    }
}

