#include <cstring>
#include <map>
#include <string>
#include <jni.h>

#include "OpenCC/src/Converter.hpp"
#include "OpenCC/src/Config.hpp"
#include "OpenCC/src/Common.hpp"
#include "OpenCC/src/DictConverter.hpp"
#include "OpenCC/src/Exception.hpp"
#include "OpenCC/src/SimpleConverter.hpp"

static inline void throwJavaException(JNIEnv *env, const char *msg) {
    jclass c = env->FindClass("java/lang/Exception");
    env->ThrowNew(c, msg);
    env->DeleteLocalRef(c);
}

class CString {
private:
    JNIEnv *env_;
    jstring str_;
    const char *chr_;

public:
    CString(JNIEnv *env, jstring str)
            : env_(env), str_(str), chr_(env->GetStringUTFChars(str, nullptr)) {}

    ~CString() { env_->ReleaseStringUTFChars(str_, chr_); }

    operator std::string() { return chr_; }

    operator const char *() { return chr_; }

    const char *operator*() { return chr_; }
};

extern "C" JNIEXPORT jstring JNICALL
Java_com_osfans_mcpdict_Util_OpenCC_openCCLineConv(
        JNIEnv *env, jclass clazz, jstring input, jstring config_file_name) {
    try {
        opencc::SimpleConverter converter(CString(env, config_file_name));
        return env->NewStringUTF(converter.Convert(*CString(env, input)).data());
    } catch (const opencc::Exception &e) {
        throwJavaException(env, e.what());
        return env->NewStringUTF("");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_osfans_mcpdict_Util_OpenCC_openCCDictConv(
        JNIEnv *env, jclass clazz, jstring src, jstring dest, jboolean mode) {
    auto src_file = CString(env, src);
    auto dest_file = CString(env, dest);
    try {
        if (mode) {
            opencc::ConvertDictionary(src_file, dest_file, "ocd2", "text");
        } else {
            opencc::ConvertDictionary(src_file, dest_file, "text", "ocd2");
        }
    } catch (const opencc::Exception &e) {
        throwJavaException(env, e.what());
    }
}