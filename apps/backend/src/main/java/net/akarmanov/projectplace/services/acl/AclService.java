package net.akarmanov.projectplace.services.acl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AclService {
    private final MutableAclService aclService;

    public MutableAcl createAcl(ObjectIdentity objectIdentity) {
        // Create a new ACL for the given object identity
        return aclService.createAcl(objectIdentity);
    }

    public void addPermission(ObjectIdentity objectIdentity, Sid sid, Permission permission) {
        // Add permission to the ACL for the given object identity and SID
        MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity);
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
        aclService.updateAcl(acl);
    }

    public void addPermissions(ObjectIdentity objectIdentity, Sid sid, List<Permission> permissions) {
        // Add permission to the ACL for the given object identity and SID
        MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity);
        for (var permission : permissions) {
            acl.insertAce(acl.getEntries().size(), permission, sid, true);
        }
        aclService.updateAcl(acl);
    }

    public void removePermission(ObjectIdentity objectIdentity, Sid sid, Permission permission) {
        // Remove permission from the ACL for the given object identity and SID
        MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity);
        List<AccessControlEntry> entries = acl.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            AccessControlEntry entry = entries.get(i);
            if (entry.getSid().equals(sid) && entry.getPermission().equals(permission)) {
                acl.deleteAce(i);
                break;
            }
        }
        aclService.updateAcl(acl);
    }

    public void createAclWithPermission(ObjectIdentity objectIdentity, String username, Permission permission) {
        // Create an ACL and add permission for a specific user
        createAcl(objectIdentity);
        Sid sid = new PrincipalSid(username);
        addPermission(objectIdentity, sid, permission);
    }
}
