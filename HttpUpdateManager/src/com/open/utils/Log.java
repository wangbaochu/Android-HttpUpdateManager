package com.open.utils;

/**
 * 包一下Log类，后面做处理
 */
public class Log {

	private static int logLevel = android.util.Log.VERBOSE;

	public static int getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(int logLevel) {
		Log.logLevel = logLevel;
	}

	/**
	 * Send a {@link android.util.Log.VERBOSE} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int v(String tag, String msg) {
		if (logLevel > android.util.Log.VERBOSE)
			return -1;
		return android.util.Log.v(tag, msg);
	}

	/**
	 * Send a {@link #VERBOSE} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int v(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.VERBOSE)
			return -1;
		return android.util.Log.v(tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	/**
	 * Send a {@link #DEBUG} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int d(String tag, String msg) {
		if (logLevel > android.util.Log.DEBUG)
			return -1;
		return android.util.Log.d(tag, msg);
	}

	/**
	 * Send a {@link #DEBUG} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int d(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.DEBUG)
			return -1;
		return android.util.Log.d(tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	/**
	 * Send an {@link #INFO} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int i(String tag, String msg) {
		if (logLevel > android.util.Log.INFO)
			return -1;
		return android.util.Log.i(tag, msg);
	}

	/**
	 * Send a {@link #INFO} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int i(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.INFO)
			return -1;
		return android.util.Log.i(tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	/**
	 * Send a {@link #WARN} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int w(String tag, String msg) {
		if (logLevel > android.util.Log.WARN)
			return -1;
		return android.util.Log.w(tag, msg);
	}

	/**
	 * Send a {@link #WARN} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int w(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.WARN) {
			return -1;
		}
		return android.util.Log.w(tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	/*
	 * Send a {@link #WARN} log message and log the exception.
	 * 
	 * @param tag Used to identify the source of a log message. It usually
	 * identifies the class or activity where the log call occurs.
	 * 
	 * @param tr An exception to log
	 */
	public static int w(String tag, Throwable tr) {
		if (logLevel > android.util.Log.WARN)
			return -1;
		return android.util.Log.w(tag, android.util.Log.getStackTraceString(tr));
	}

	/**
	 * Send an {@link #ERROR} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int e(String tag, String msg) {
		if (logLevel > android.util.Log.ERROR)
			return -1;
		return android.util.Log.e(tag, msg);
	}

	/**
	 * Send a {@link #ERROR} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int e(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.ERROR)
			return -1;
		return android.util.Log.e(tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	/**
	 * What a Terrible Failure: Report a condition that should never happen. The
	 * error will always be logged at level ASSERT with the call stack.
	 * Depending on system configuration, a report may be added to the
	 * {@link android.os.DropBoxManager} and/or the process may be terminated
	 * immediately with an error dialog.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int wtf(String tag, String msg) {
		if (logLevel > android.util.Log.ASSERT)
			return -1;
		return android.util.Log.wtf(tag, msg);
	}

	/**
	 * What a Terrible Failure: Report an exception that should never happen.
	 * Similar to {@link #wtf(String, String)}, with an exception to log.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message.
	 * @param tr
	 *            An exception to log.
	 */
	public static int wtf(String tag, Throwable tr) {
		if (logLevel > android.util.Log.ASSERT)
			return -1;
		return android.util.Log.wtf(tag, tr.getMessage(), tr);
	}

	/**
	 * What a Terrible Failure: Report an exception that should never happen.
	 * Similar to {@link #wtf(String, Throwable)}, with a message as well.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log. May be null.
	 */
	public static int wtf(String tag, String msg, Throwable tr) {
		if (logLevel > android.util.Log.ASSERT)
			return -1;
		return android.util.Log.wtf(tag, msg, tr);
	}
}
