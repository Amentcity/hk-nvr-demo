package com.example.hknvr.util;

import com.sun.jna.Platform;

import java.io.File;

/**
 * 根据操作系统解析海康 SDK 本地库路径。
 */
public final class OsUtils {

    private OsUtils() {
    }

    public static boolean isWindows() {
        return Platform.isWindows();
    }

    public static boolean isLinux() {
        return Platform.isLinux();
    }

    /**
     * 返回 HCNetSDK 库加载名（不含扩展名）。
     */
    public static String getSdkLibraryName() {
        if (Platform.isWindows()) {
            return "HCNetSDK";
        }
        if (Platform.isLinux()) {
            return "libhcnetsdk.so";
        }
        throw new UnsupportedOperationException("Unsupported OS for Hikvision SDK");
    }

    /**
     * 根据 OS 返回默认 SDK 子目录名。
     */
    public static String getDefaultLibSubDir() {
        if (Platform.isWindows()) {
            return Platform.is64Bit() ? "win64" : "win32";
        }
        if (Platform.isLinux()) {
            return Platform.is64Bit() ? "linux64" : "linux32";
        }
        throw new UnsupportedOperationException("Unsupported OS for Hikvision SDK");
    }

    /**
     * 将 libPath 解析为绝对路径并校验 HCNetSDK 库文件是否存在。
     */
    public static File resolveSdkLibDir(String libPath) {
        File dir = new File(libPath);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), libPath);
        }
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("SDK lib directory not found: " + dir.getAbsolutePath());
        }
        String libFileName = isWindows() ? "HCNetSDK.dll" : "libhcnetsdk.so";
        File libFile = new File(dir, libFileName);
        if (!libFile.exists()) {
            throw new IllegalStateException(
                    "SDK library not found: " + libFile.getAbsolutePath()
                            + ". Please copy Hikvision Device Network SDK files here.");
        }
        return dir;
    }
}
