package com.berryfi.portal.config;

import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import com.berryfi.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initialization class to create sample users.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create sample users if they don't exist
        createSampleUsers();
    }

    private void createSampleUsers() {
        // Create Super Admin user
        if (!userRepository.existsByEmail("admin@berryfi.com")) {
            User superAdmin = new User();
            superAdmin.setName("Super Admin");
            superAdmin.setEmail("admin@berryfi.com");
            superAdmin.setPassword(passwordEncoder.encode("SuperSecurePassword@123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setAccountType(AccountType.ORGANIZATION);
            superAdmin.setOrganizationId("berryfi");
            superAdmin.setStatus(UserStatus.ACTIVE);
            userRepository.save(superAdmin);
        }

        // Create Organization Owner (Mithesh Bhat)
        if (!userRepository.existsByEmail("mithesh@ravgroup.org")) {
            User orgOwner = new User();
            orgOwner.setName("Mithesh Bhat");
            orgOwner.setEmail("mithesh@ravgroup.org");
            orgOwner.setPassword(passwordEncoder.encode("password123"));
            orgOwner.setRole(Role.ORG_OWNER);
            orgOwner.setAccountType(AccountType.ORGANIZATION);
            orgOwner.setOrganizationId("ravgroup");
            orgOwner.setStatus(UserStatus.ACTIVE);
            userRepository.save(orgOwner);
        }

        // Create Organization Admin
        if (!userRepository.existsByEmail("admin@ravgroup.org")) {
            User orgAdmin = new User();
            orgAdmin.setName("Organization Admin");
            orgAdmin.setEmail("admin@ravgroup.org");
            orgAdmin.setPassword(passwordEncoder.encode("password123"));
            orgAdmin.setRole(Role.ORG_ADMIN);
            orgAdmin.setAccountType(AccountType.ORGANIZATION);
            orgAdmin.setOrganizationId("ravgroup");
            orgAdmin.setStatus(UserStatus.ACTIVE);
            userRepository.save(orgAdmin);
        }

        // Create Workspace Admin
        if (!userRepository.existsByEmail("workspace.admin@apexmarketing.com")) {
            User workspaceAdmin = new User();
            workspaceAdmin.setName("Workspace Admin");
            workspaceAdmin.setEmail("workspace.admin@apexmarketing.com");
            workspaceAdmin.setPassword(passwordEncoder.encode("password123"));
            workspaceAdmin.setRole(Role.WORKSPACE_ADMIN);
            workspaceAdmin.setAccountType(AccountType.WORKSPACE);
            workspaceAdmin.setOrganizationId("ravgroup");
            workspaceAdmin.setWorkspaceId("apexmarketing");
            workspaceAdmin.setStatus(UserStatus.ACTIVE);
            userRepository.save(workspaceAdmin);
        }

        // Create Organization Member
        if (!userRepository.existsByEmail("member@ravgroup.org")) {
            User orgMember = new User();
            orgMember.setName("Organization Member");
            orgMember.setEmail("member@ravgroup.org");
            orgMember.setPassword(passwordEncoder.encode("password123"));
            orgMember.setRole(Role.ORG_MEMBER);
            orgMember.setAccountType(AccountType.ORGANIZATION);
            orgMember.setOrganizationId("ravgroup");
            orgMember.setStatus(UserStatus.ACTIVE);
            userRepository.save(orgMember);
        }

        // Create Workspace Member
        if (!userRepository.existsByEmail("member@apexmarketing.com")) {
            User workspaceMember = new User();
            workspaceMember.setName("Workspace Member");
            workspaceMember.setEmail("member@apexmarketing.com");
            workspaceMember.setPassword(passwordEncoder.encode("password123"));
            workspaceMember.setRole(Role.WORKSPACE_MEMBER);
            workspaceMember.setAccountType(AccountType.WORKSPACE);
            workspaceMember.setOrganizationId("ravgroup");
            workspaceMember.setWorkspaceId("apexmarketing");
            workspaceMember.setStatus(UserStatus.ACTIVE);
            userRepository.save(workspaceMember);
        }

        System.out.println("Sample users created successfully!");
        System.out.println("Available test users:");
        System.out.println("1. Super Admin: admin@berryfi.com / SuperSecurePassword@123");
        System.out.println("2. Org Owner: mithesh@ravgroup.org / password123");
        System.out.println("3. Org Admin: admin@ravgroup.org / password123");
        System.out.println("4. Workspace Admin: workspace.admin@apexmarketing.com / password123");
        System.out.println("5. Org Member: member@ravgroup.org / password123");
        System.out.println("6. Workspace Member: member@apexmarketing.com / password123");
    }
}
