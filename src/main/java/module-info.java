module tello {
    requires javafx.controls;
    requires java.logging;

    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.ffmpeg.macosx.arm64;
//    requires org.bytedeco.ffmpeg.macosx.x86_64;
    requires org.bytedeco.ffmpeg.windows.x86_64;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires java.desktop;

    exports tello;
    exports telloflix;
}