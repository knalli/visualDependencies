package de.unibonn.inf.dbdependenciesui.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * This class provides a systemwide executor service.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ThreadExecutor {
	private static ExecutorService executor;

	static {
		executor = Executors.newFixedThreadPool(2);
	}

	/**
	 * Executes the given command at some time in the future. The command may execute in a new thread, in a pooled
	 * thread, or in the calling thread, at the discretion of the <tt>Executor</tt> implementation.
	 * 
	 * @param command
	 *            the runnable task
	 * @throws RejectedExecutionException
	 *             if this task cannot be accepted for execution.
	 * @throws NullPointerException
	 *             if command is null
	 */
	public static void execute(final Runnable command) {
		executor.execute(command);
	}
}
