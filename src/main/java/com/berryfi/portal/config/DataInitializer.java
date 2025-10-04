package com.berryfi.portal.config;

import com.berryfi.portal.entity.Organization;
import com.berryfi.portal.entity.User;
import com.berryfi.portal.enums.AccountType;
import com.berryfi.portal.enums.OrganizationStatus;
import com.berryfi.portal.enums.Role;
import com.berryfi.portal.enums.UserStatus;
import com.berryfi.portal.repository.OrganizationRepository;
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
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create sample organizations first (users reference these)
        createSampleOrganizations();
        // Create sample users if they don't exist
        createSampleUsers();
    }

    private void createSampleOrganizations() {
        // Create BerryFi organization for super admin
        if (!organizationRepository.existsById("berryfi")) {
            Organization berryfiOrg = new Organization(
                "BerryFi Systems",
                "Primary BerryFi organization for system administration",
                "system", // Will be updated when user is created
                "admin@berryfi.com",
                "Super Admin",
                "system"
            );
            berryfiOrg.setId("berryfi");
            berryfiOrg.setStatus(OrganizationStatus.ACTIVE);
            berryfiOrg.setTotalCredits(10000.0);
            berryfiOrg.setRemainingCredits(10000.0);
            organizationRepository.save(berryfiOrg);
        }

        // Create RAV Group organization
        if (!organizationRepository.existsById("ravgroup")) {
            Organization ravgroupOrg = new Organization(
                "RAV Group",
                "Sample organization for testing purposes",
                "system", // Will be updated when user is created
                "mithesh@ravgroup.org",
                "Mithesh Bhat",
                "system"
            );
            ravgroupOrg.setId("ravgroup");
            ravgroupOrg.setStatus(OrganizationStatus.ACTIVE);
            ravgroupOrg.setTotalCredits(5000.0);
            ravgroupOrg.setRemainingCredits(5000.0);
            organizationRepository.save(ravgroupOrg);
        }
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
            User savedSuperAdmin = userRepository.save(superAdmin);
            
            // Update organization owner ID
            organizationRepository.findById("berryfi").ifPresent(org -> {
                org.setOwnerId(savedSuperAdmin.getId());
                organizationRepository.save(org);
            });
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
            User savedOrgOwner = userRepository.save(orgOwner);
            
            // Update organization owner ID
            organizationRepository.findById("ravgroup").ifPresent(org -> {
                org.setOwnerId(savedOrgOwner.getId());
                organizationRepository.save(org);
            });
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



        System.out.println("Sample users created successfully!");
        System.out.println("Available test users:");
        System.out.println("1. Super Admin: admin@berryfi.com / SuperSecurePassword@123");
        System.out.println("2. Org Owner: mithesh@ravgroup.org / password123");
        System.out.println("3. Org Admin: admin@ravgroup.org / password123");
        System.out.println("4. Org Member: member@ravgroup.org / password123");
    }
}
