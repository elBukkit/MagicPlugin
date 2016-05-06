package com.elmakers.mine.bukkit.utility;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

public class NMSUtilsMocker {

    public static void initializeNMSUtilsMocking() {
        when(NMSUtils.getFailed()).thenReturn(false);

    }
}
