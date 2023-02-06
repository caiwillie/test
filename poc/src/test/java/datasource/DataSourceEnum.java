package datasource;

import lombok.Getter;

@Getter
public enum DataSourceEnum {
    POC("jdbc:mysql://10.101.3.39:3306/mop?useUnicode=true&useSSL=false&characterEncoding=utf8",
            "root", "test123456");

    private final String jdbcUrl;
    private final String user;
    private final String password;

    DataSourceEnum(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }
}
