/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class MemberModelInfo implements Serializable {
	public LinkedHashMap<Integer, MemberModel> members_list = new LinkedHashMap<Integer, MemberModel>();
}
