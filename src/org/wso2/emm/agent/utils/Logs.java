/**
 * @author jugo
 * @date 2013-09-30
 * @description common log module
 */

package org.wso2.emm.agent.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import java.lang.Throwable;

public class Logs
{
	private static final int	mnEnable		= 0;
	private static final int	mnTraceLevel	= 1;

	private static class LogInfo
	{
		String	strFile			= "";
		String	strClassPath	= "";
		String	strClassName	= "";
		String	strMethod		= "";
		int		nLine			= -1;
	}

	private static LogInfo	logInfo	= new LogInfo();

	@SuppressWarnings("unused")
	public static void showTrace(String msg)
	{
		if (-1 == mnEnable)
		{
			Log.d("Logs", "Logs is Invalid");
			return;
		}

		if (msg.length() > 0)
		{
			Throwable throwable = new Throwable();
			logInfo.strFile = throwable.getStackTrace()[1].getFileName();
			logInfo.strClassPath = throwable.getStackTrace()[1].getClassName();
			logInfo.strClassName = extractSimpleClassName(logInfo.strClassPath);
			logInfo.strMethod = throwable.getStackTrace()[1].getMethodName();
			logInfo.nLine = throwable.getStackTrace()[1].getLineNumber();
			throwable = null;
			String strLog = null;
			switch (mnTraceLevel)
			{
			case 1:
				strLog = "[TRACE] " + msg;
				break;
			case 2:
				strLog = "[TRACE] " + " class: " + logInfo.strClassPath + " line: " + logInfo.nLine + " Msg: " + msg;
				break;
			case 3:
				strLog = "[TRACE] " + "file: " + logInfo.strFile + " class: " + logInfo.strClassPath + " method: "
						+ logInfo.strMethod + " line: " + logInfo.nLine + " Msg: " + msg;
				break;
			default:
				strLog = "[TRACE] " + msg;
				break;
			}

			Log.d(logInfo.strClassName, strLog);
		}
	}

	public static String extractSimpleClassName(String fullClassName)
	{
		if ((null == fullClassName) || ("".equals(fullClassName)))
		{
			return "";
		}
		int lastDot = fullClassName.lastIndexOf('.');
		if (0 > lastDot)
			return fullClassName;
		return fullClassName.substring(++lastDot);
	}

	public Logs()
	{

	}

	public static void complain(Context context, String message)
	{
		AlertDialog.Builder bld = new AlertDialog.Builder(context);
		bld.setMessage("Error : " + message);
		bld.setNeutralButton("OK", null);
		bld.create().show();
	}
}
