#include "pmonitor.h"
#include "process.h"

static jint Data_Load(JNIEnv* env, jobject thiz, jint type)
{
	int nCount = 0;

	switch (type)
	{
	case 1: // process
		nCount = ps_dump();
		break;
	}

	return nCount;
}

static jint PS_Count(JNIEnv* env, jobject thiz)
{
	return ps_list_count();
}

static void release()
{
	ps_release();
}

static jstring PS_Name(JNIEnv* env, jobject thiz, jint position)
{
	char buf[BUFFERSIZE];
	ps_get_name(position, buf);
	return (*env)->NewStringUTF(env, buf);
}

static jobjectArray PS_Info(JNIEnv* env, jobject thiz, jint position)
{
	char bufName[BUFFERSIZE];
	char bufStatue[BUFFERSIZE];
	jobjectArray ret;
	int nRet = 0;

	nRet = ps_info(position, bufName, bufStatue);

	ret = (jobjectArray)(*env)->NewObjectArray(env, 5,
			(*env)->FindClass(env, "java/lang/String"),
			(*env)->NewStringUTF(env, ""));
	if (0 < nRet)
	{
//		char *message[5] =
//		{ "first", "second", "third", "fourth", "fifth" };

		(*env)->SetObjectArrayElement(env, ret, 0,
				(*env)->NewStringUTF(env, bufName));
		(*env)->SetObjectArrayElement(env, ret, 1,
				(*env)->NewStringUTF(env, bufStatue));
		/*	for (int i = 0; i < 5; i++)
		 {
		 (*env)->SetObjectArrayElement(env, ret, i,
		 (*env)->NewStringUTF(env, message[i]));
		 }
		 */
	}
	return (ret);
}

/**  JNI Load and UnLoad **/
static const char *classPathName = "dev/jugo/processmonitor/JNIInterface";
static JNINativeMethod gMethods[] =
{
{ "doDataLoad", "(I)I", Data_Load },
{ "GetProcessCounts", "()I", PS_Count },
{ "GetProcessName", "(I)Ljava/lang/String;", PS_Name },
{ "GetProcessInfo", "(I)[Ljava/lang/String;", PS_Info } };

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;

	if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4))
		return JNI_ERR;

	cls = (*env)->FindClass(env, classPathName);

	(*env)->RegisterNatives(env, cls, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0]));

	return JNI_VERSION_1_4;
}

void JNI_OnUnload(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;

	if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4))
		return;

	cls = (*env)->FindClass(env, classPathName);
	(*env)->UnregisterNatives(env, cls);

	release();
	return;
}

