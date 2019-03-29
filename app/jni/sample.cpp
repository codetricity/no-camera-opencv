#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv/cv.hpp>

extern "C"
{
    JNIEXPORT jstring JNICALL
    Java_com_example_opencvsample_MainActivity_version(
            JNIEnv *env,
            jobject) {
        std::string version = cv::getVersionString();
        return env->NewStringUTF(version.c_str());
    }

    JNIEXPORT jbyteArray
    JNICALL Java_com_example_opencvsample_MainActivity_rgba2bgra
            (
                    JNIEnv *env,
                    jobject obj,
                    jint w,
                    jint h,
                    jbyteArray src
            ) {
        // Obtaining element row
        // Need to release at the end
        jbyte *p_src = env->GetByteArrayElements(src, NULL);
        if (p_src == NULL) {
            return NULL;
        }

        // Convert arrangement to cv::Mat
        cv::Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
        cv::Mat m_dst(h, w, CV_8UC4);

        // OpenCV process
        cv::cvtColor(m_src, m_dst, CV_RGBA2BGRA);

        // Pick out arrangement from cv::Mat
        u_char *p_dst = m_dst.data;

        // Assign element for return value use
        jbyteArray dst = env->NewByteArray(w * h * 4);
        if (dst == NULL) {
            env->ReleaseByteArrayElements(src, p_src, 0);
            return NULL;
        }
        env->SetByteArrayRegion(dst, 0, w * h * 4, (jbyte *) p_dst);

        // release
        env->ReleaseByteArrayElements(src, p_src, 0);

        return dst;
    }
}