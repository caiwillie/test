package com.brandnewdata.mop.poc.scene.bo.export;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConnectorExportBo {
    private String connectorGroup;
    private String connectorId;
    private String connectorVersion;

    private String connectorName;

    private String connectorSmallIcon;

    private List<ConfigExportBo> configurations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectorExportBo that = (ConnectorExportBo) o;

        if (!connectorGroup.equals(that.connectorGroup)) return false;
        if (!connectorId.equals(that.connectorId)) return false;
        return connectorVersion.equals(that.connectorVersion);
    }

    @Override
    public int hashCode() {
        int result = connectorGroup.hashCode();
        result = 31 * result + connectorId.hashCode();
        result = 31 * result + connectorVersion.hashCode();
        return result;
    }
}
