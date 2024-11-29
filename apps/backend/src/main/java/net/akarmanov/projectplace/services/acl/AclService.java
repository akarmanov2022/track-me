package net.akarmanov.projectplace.services.acl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.domain.TeamCard;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AclService {
    private final MutableAclService aclService;

    public MutableAcl createAcl(Object identity) {
        var objectIdentity = new ObjectIdentityImpl(identity);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var sid = new PrincipalSid(authentication);
        aclService.createAcl(objectIdentity);
        return addPermissions(objectIdentity, sid, List.of(
                BasePermission.READ,
                BasePermission.WRITE,
                BasePermission.DELETE));
    }

    public void createAcl(Object identity, String principle) {
        var objectIdentity = new ObjectIdentityImpl(identity);
        var sid = new PrincipalSid(principle);
        aclService.createAcl(objectIdentity);
        addPermissions(objectIdentity, sid, List.of(
                BasePermission.READ,
                BasePermission.WRITE,
                BasePermission.DELETE));
    }

    public void updateAcl(Object identity, String principle) {
        var objectIdentity = new ObjectIdentityImpl(identity);
        var sid = new PrincipalSid(principle);
        var acl = (MutableAcl) aclService.readAclById(objectIdentity);
        acl.setOwner(sid);
        aclService.updateAcl(acl);
    }

    public void addPermission(ObjectIdentity objectIdentity, Sid sid, Permission permission) {
        // Add permission to the ACL for the given object identity and SID
        MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity);
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
        aclService.updateAcl(acl);
    }

    public MutableAcl addPermissions(ObjectIdentity objectIdentity, Sid sid, List<Permission> permissions) {
        // Add permission to the ACL for the given object identity and SID
        MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity);
        acl.setOwner(sid);
        for (var permission : permissions) {
            acl.insertAce(acl.getEntries().size(), permission, sid, true);
        }
        return aclService.updateAcl(acl);
    }

    public void createAclWithParent(Object identity, Object parent) {
        var parentObjectIdentity = new ObjectIdentityImpl(parent);
        var acl = createAcl(identity);
        var parentAcl = (MutableAcl) aclService.readAclById(parentObjectIdentity);
        acl.setParent(parentAcl);
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

    public void deleteAcl(TeamCard teamCard) {
        var objectIdentity = new ObjectIdentityImpl(teamCard);
        aclService.deleteAcl(objectIdentity, true);
    }
}
