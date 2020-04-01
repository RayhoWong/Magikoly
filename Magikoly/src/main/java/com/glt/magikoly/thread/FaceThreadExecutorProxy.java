package com.glt.magikoly.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import com.cs.framework.core.thread.AbstractThreadExecutor;
import com.cs.framework.core.thread.ThreadPoolManager;
import com.cs.statistic.utiltool.CpuManager;

import java.util.concurrent.TimeUnit;

/**
 * GO桌面线程执行代理类，用于统一管理线程
 * @author yangguanxiang
 *
 */
public class FaceThreadExecutorProxy {

	private final static String POOL_NAME = "face_thread_pool";
	private final static int DEFAULT_CORE_POOL_SIZE = 2;
	private final static int DEFAULT_MAX_POOL_SIZE = 6;
	private final static int KEEP_ALIVE_TIME = 60;
	private final static String ASYNC_THREAD_NAME = "face-single-async-thread";
	private static GoLauncherThreadExecutor sExecutor;
	private static boolean sIsInit;
	private static int sCorePoolSize = DEFAULT_CORE_POOL_SIZE;

	private static HandlerThread sSingleAsyncThread;

	private static Handler sSingleAsyncHandler;

	private static Handler sMainHandler;

	public static MessageQueue sMsgQueue;

	public static void init() {
		if (sIsInit) {
			return;
		}
		sCorePoolSize = CpuManager.getNumCores();
		if (sCorePoolSize < DEFAULT_CORE_POOL_SIZE) {
			sCorePoolSize = DEFAULT_CORE_POOL_SIZE;
		}
		if (sCorePoolSize > DEFAULT_MAX_POOL_SIZE) {
			sCorePoolSize = DEFAULT_MAX_POOL_SIZE;
		}
		sExecutor = new GoLauncherThreadExecutor();

		sSingleAsyncThread = new HandlerThread(ASYNC_THREAD_NAME);
		sSingleAsyncThread.start();
		sSingleAsyncHandler = new Handler(sSingleAsyncThread.getLooper());

		sMainHandler = new Handler(Looper.getMainLooper());

		sMsgQueue = Looper.myQueue();
		sIsInit = true;
	}

	/**
	 * 提交异步任务到线程池中执行
	 * @param task 需要执行的任务
	 */
	public static void execute(Runnable task) {
		sExecutor.execute(task);
	}

	/**
	 * 提交异步任务到线程池中执行
	 * @param task 需要执行的任务
	 * @param threadName 线程名称
	 */
	public static void execute(Runnable task, String threadName) {
		sExecutor.execute(task, threadName);
	}

	/**
	 * 提交异步任务到线程池中执行
	 * @param task 需要执行的任务
	 * @param priority 线程优先级，该值来自于Thread，不是来自于Process；
	 * 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	 * Android建议使用OS层级设置优先级，效果更显著
	 */
	public static void execute(Runnable task, int priority) {
		sExecutor.execute(task, priority);
	}

	/**
	 * 提交异步任务到线程池中执行
	 * @param task 需要执行的任务
	 * @param threadName 线程名称
	 * @param priority 线程优先级，该值来自于Thread，不是来自于Process；
	 * 如需要设置OS层级的优先级，可以在task.run方法开头调用如Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	 * Android建议使用OS层级设置优先级，效果更显著
	 */
	public static void execute(Runnable task, String threadName, int priority) {
		sExecutor.execute(task, threadName, priority);
	}

	/**
	 * 取消指定的任务
	 * @param task
	 */
	public static void cancel(final Runnable task) {
		sExecutor.cancel(task);
		sSingleAsyncHandler.removeCallbacks(task);
		sMainHandler.removeCallbacks(task);
	}

	/**
	 * 销毁对象
	 */
	public static void destroy() {
		sExecutor.destroy();
		sSingleAsyncHandler.removeCallbacksAndMessages(null);
		sMainHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * 提交一个Runable到异步线程队列，该异步线程为单队列
	 * 
	 * @param r
	 */
	public static void runOnAsyncThread(Runnable r) {
		sSingleAsyncHandler.post(r);
	}

	/**
	 * 提交一个Runable到异步线程队列，该异步线程为单队列
	 * @param r
	 * @param delay
	 */
	public static void runOnAsyncThread(Runnable r, long delay) {
		sSingleAsyncHandler.postDelayed(r, delay);
	}

	/**
	 * 提交一个Runable到主线程队列
	 * 
	 * @param r
	 */
	public static void runOnMainThread(Runnable r) {
		sMainHandler.post(r);
	}

	/**
	 * 提交一个Runable到主线程队列
	 * @param r
	 * @param delay
	 */
	public static void runOnMainThread(Runnable r, long delay) {
		sMainHandler.postDelayed(r, delay);
	}

	/**
	 * 提交一个Runnable到主线程空闲时执行
	 * @param r
	 */
	public static void runOnIdleTime(final Runnable r) {
		IdleHandler handler = new IdleHandler() {

			@Override
			public boolean queueIdle() {
				r.run();
				return false;
			}
		};
		sMsgQueue.addIdleHandler(handler);
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	private static class GoLauncherThreadExecutor extends AbstractThreadExecutor {

		private GoLauncherThreadExecutor() {

		}

		@Override
		protected ThreadPoolManager initThreadPoolManager() {
			ThreadPoolManager manager = ThreadPoolManager.buildInstance(POOL_NAME, sCorePoolSize,
					DEFAULT_MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, false,
					getTaskExecuteListener());
			manager.allowCoreThreadTimeOut(true);
			return manager;
		}
	}
}
