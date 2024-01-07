/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

/**
 * リトライオーバー例外クラス.
 * <P>
 * リトライオーバー発生時に投げる例外
 */
public class RetryOverException extends Exception {

	public RetryOverException(String message) {
		super(message);
	}

}
