package org.wso2.emm.agent.api;

import org.wso2.emm.agent.utils.Logs;

import android.content.Context;

public class ProcessInfo
{
	private Context			theContext		= null;
	private JNIInterface	jniinterface	= null;

	public ProcessInfo(Context context)
	{
		theContext = context;
		jniinterface = JNIInterface.getInstance();
	}
	
	public void getProcessInfo()
	{
		int nCount = jniinterface.doDataLoad(1);
		Logs.showTrace(String.format("process count:%d  #####################", nCount));
		String[] strProcList = null;
		for (int i = 0; i < nCount; ++i)
		{
			strProcList = jniinterface.GetProcessInfo(i);
			Logs.showTrace("Process Name: " + strProcList[0] + " Status: " + strProcList[1]);
		}
	}
}
