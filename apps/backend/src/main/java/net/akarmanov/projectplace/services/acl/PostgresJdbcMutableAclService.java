package net.akarmanov.projectplace.services.acl;

import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;

import javax.sql.DataSource;

public class PostgresJdbcMutableAclService extends JdbcMutableAclService {
    public PostgresJdbcMutableAclService(DataSource dataSource,
                                         LookupStrategy lookupStrategy,
                                         AclCache aclCache) {
        super(dataSource, lookupStrategy, aclCache);
        super.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
        super.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
        super.setAclClassIdSupported(true);
    }
}
