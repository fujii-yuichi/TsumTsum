/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmuInstanceModelInfo implements Serializable {
    public List<EmuInstanceModel> instance_list = new ArrayList<EmuInstanceModel>();

    public int size() {
        return instance_list.size();
    }

    public EmuInstanceModel get(int index) {
        return instance_list.get(index);
    }
}