#include <jni.h>

extern "C"
{
void Java_cos_mos_recorder_decode_ULame_close(JNIEnv *env, jclass type);

jint Java_cos_mos_recorder_decode_ULame_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                                               jshortArray buffer_r_, jint samples,
                                               jbyteArray mp3buf_);

jint Java_cos_mos_recorder_decode_ULame_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_);

void Java_cos_mos_recorder_decode_ULame_init(JNIEnv *env, jclass type, jint inSampleRate,
                                                    jint outChannel, jint outSampleRate,
                                                    jint outBitrate, jint quality);
}