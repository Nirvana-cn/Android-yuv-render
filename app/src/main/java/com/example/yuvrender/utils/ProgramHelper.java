package com.example.yuvrender.utils;

import android.opengl.GLES20;
import android.util.Log;

public class ProgramHelper {
    private static final String TAG = "ProgramHelper";

    /**
     * 创建OpenGL程序对象
     *
     * @param vertexShader   顶点着色器代码
     * @param fragmentShader 片段着色器代码
     */
    public static int makeProgram(String vertexShader, String fragmentShader) {
        // 步骤1：编译顶点着色器
        int vertexShaderId = ShaderHelper.compileVertexShader(vertexShader);
        // 步骤2：编译片段着色器
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShader);
        // 步骤3：将顶点着色器、片段着色器进行链接，组装成一个OpenGL程序
        int mProgram = linkProgram(vertexShaderId, fragmentShaderId);

        validateProgram(mProgram);

        // 步骤4：通知OpenGL开始使用该程序
        GLES20.glUseProgram(mProgram);

        return mProgram;
    }

    /**
     * 创建OpenGL程序：通过链接顶点着色器、片段着色器
     *
     * @param vertexShaderId   顶点着色器ID
     * @param fragmentShaderId 片段着色器ID
     * @return OpenGL程序ID
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // 1.创建一个OpenGL程序对象
        final int programObjectId = GLES20.glCreateProgram();

        // 2.获取创建状态
        if (programObjectId == 0) {
            Log.w(TAG, "Could not create new program");
            return 0;
        }

        // 3.将顶点着色器依附到OpenGL程序对象
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        // 3.将片段着色器依附到OpenGL程序对象
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        // 4.将两个着色器链接到OpenGL程序对象
        GLES20.glLinkProgram(programObjectId);

        // 5.获取链接状态：OpenGL将想要获取的值放入长度为1的数组的首位
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        Log.v(TAG, "Results of linking program:\n" + GLES20.glGetProgramInfoLog(programObjectId));

        // 6.验证链接状态
        if (linkStatus[0] == 0) {
            // 链接失败则删除程序对象
            GLES20.glDeleteProgram(programObjectId);
            Log.w(TAG, "Linking of program failed.");
            // 7.返回程序对象：失败，为0
            return 0;
        }

        // 7.返回程序对象：成功，非0
        return programObjectId;
    }

    /**
     * 验证OpenGL程序对象状态
     *
     * @param programObjectId OpenGL程序ID
     * @return 是否可用
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0] + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }
}


