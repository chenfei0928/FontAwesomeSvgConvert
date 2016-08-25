package io.mrfeng.fontAwsome;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MyClass {
    private static final String APP_ROOT_PATH = "C:\\Users\\MrFeng\\AndroidStudioProjects\\yike_android\\app\\src\\main";
    private static final String WEBP_ENCODE = "C:\\Users\\MrFeng\\Desktop\\libwebp-0.5.1-windows-x64\\bin\\cwebp.exe";

    public static void main(String[] args) {
        Arrays.asList(
                new File(APP_ROOT_PATH, "res")
                        .listFiles((file, s) -> s.startsWith("drawable") || s.startsWith("mipmap"))
        )
                .parallelStream()
                .flatMap(file ->
                        Arrays.asList(file.listFiles((file1, s) -> !s.endsWith("\\.webp") && !s.endsWith("\\.xml")))
                                .parallelStream()
                )
                .forEach(file -> {
                    try {
                        Runtime.getRuntime().exec(WEBP_ENCODE + " -q 80 "
                                + file.getAbsoluteFile()
                                + " -o "
                                + file.getParentFile().getAbsolutePath() + File.separatorChar
                                + file.getName().substring(0, file.getName().lastIndexOf('.')) + ".webp").waitFor();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    file.delete();
                });
    }
}
