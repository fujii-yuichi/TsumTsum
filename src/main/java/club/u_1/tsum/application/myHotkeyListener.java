/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.util.Objects;

import org.sikuli.basics.HotkeyEvent;
import org.sikuli.basics.HotkeyListener;

public class myHotkeyListener extends HotkeyListener {

	@Override
	public void hotkeyPressed(HotkeyEvent arg0) {
		my.println("Exiting by hotkey");
		myLogger.close();
		if (Objects.nonNull(my.r)) {
			my.r.waitForIdle();
			my.sleep(100);
		}
		my.println("Exit");
		my.exit(0);
	}

}
