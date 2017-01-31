package org.bbmri.podium.domain.util;

import org.hibernate.dialect.H2Dialect;

public class FixedH2Dialect extends H2Dialect {
    public FixedH2Dialect() {
        this.registerColumnType(6, "real");
    }
}
