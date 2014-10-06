package org.wso2.emm.agent.api;

public class JNIInterface
{
	private static JNIInterface	instance	= null;

	public static JNIInterface getInstance()
	{
		if (null == instance)
		{
			System.loadLibrary("pmonitor");
			instance = new JNIInterface();
		}
		return instance;
	}

	/** process **/
	public native int doDataLoad(int nType);

	public native int GetProcessCounts();

	public native String GetProcessName(int position);

	public native String[] GetProcessInfo(int position);
}
