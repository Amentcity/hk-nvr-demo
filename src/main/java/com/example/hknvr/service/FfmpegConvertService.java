package com.example.hknvr.service;

import com.example.hknvr.config.HikvisionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FfmpegConvertService {

    private final HikvisionProperties properties;

    public FfmpegConvertService(HikvisionProperties properties) {
        this.properties = properties;
    }

    /**
     * 将海康 SDK 下载的 PS/DAV 文件转为 MP4。
     *
     * @return 转换后的 MP4 绝对路径；失败则返回 null
     */
    public String convertToMp4(String sourcePath) {
        File source = new File(sourcePath);
        if (!source.exists()) {
            log.warn("Source file not found for conversion: {}", sourcePath);
            return null;
        }

        String mp4Path = sourcePath.replaceAll("\\.(ps|dav|mpg|mpeg)$", ".mp4");
        if (mp4Path.equals(sourcePath)) {
            mp4Path = sourcePath + ".mp4";
        }

        ProcessBuilder pb = new ProcessBuilder(
                properties.getFfmpeg().getPath(),
                "-y",
                "-i", source.getAbsolutePath(),
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-an",
                mp4Path
        );

        try {
            Process process = pb.start();
            logFfmpegOutput(process);
            boolean finished = process.waitFor(30, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.error("FFmpeg conversion timeout: {}", sourcePath);
                return null;
            }
            if (process.exitValue() != 0) {
                log.error("FFmpeg conversion failed, exit={}", process.exitValue());
                return null;
            }
            log.info("Converted {} -> {}", sourcePath, mp4Path);
            return new File(mp4Path).getAbsolutePath();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FFmpeg conversion error: {}", e.getMessage());
            return null;
        }
    }

    private void logFfmpegOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[ffmpeg] {}", line);
                }
            } catch (IOException ignored) {
            }
        }, "ffmpeg-log").start();
    }
}
