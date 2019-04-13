#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

extern "C"
{
    JNIEXPORT jstring JNICALL
    Java_com_example_opencvsample_MainActivity_version(
            JNIEnv *env,
            jobject) {
        string version = getVersionString();
        return env->NewStringUTF(version.c_str());
    }

    JNIEXPORT jbyteArray
    JNICALL Java_com_example_opencvsample_MainActivity_bilateral
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
        Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
        Mat m_int(h, w, CV_8UC3);
        Mat m_dst(h, w, CV_8UC3);

        // OpenCV process
        cvtColor(m_src, m_int, COLOR_RGBA2RGB);
        bilateralFilter(m_int, m_dst, 15, 80, 80, BORDER_DEFAULT);
        cvtColor(m_dst, m_src, COLOR_RGB2RGBA);

        // Pick out arrangement from Mat
        u_char *p_dst = m_src.data;

        // Assign element for return value use
        jbyteArray dst = env->NewByteArray(w * h * 4);
        if (dst == NULL) {
            env->ReleaseByteArrayElements(src, p_src, 0);
            return NULL;
        }
        env->SetByteArrayRegion(dst, 0, w * h * 4, (jbyte *) p_src);

        // release
        env->ReleaseByteArrayElements(src, p_src, 0);

        return dst;
    }
}