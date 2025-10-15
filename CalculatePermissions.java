import java.util.List;

public class CalculatePermissions {
    
    public static void main(String[] args) {
        System.out.println("\n=== Role Permission Values (with BUILD permissions) ===\n");
        
        // Calculate each role
        calculateRole("Owner");
        calculateRole("Admin");
        calculateRole("Developer");
        calculateRole("Analyst");
        calculateRole("Support");
    }
    
    private static void calculateRole(String roleName) {
        long permissions = 0L;
        
        switch (roleName) {
            case "Owner":
                permissions = getOwnerPermissions();
                break;
            case "Admin":
                permissions = getAdminPermissions();
                break;
            case "Developer":
                permissions = getDeveloperPermissions();
                break;
            case "Analyst":
                permissions = getAnalystPermissions();
                break;
            case "Support":
                permissions = getSupportPermissions();
                break;
        }
        
        System.out.println(roleName + " role:");
        System.out.println("  Permission value: " + permissions);
        System.out.println("  Binary: " + Long.toBinaryString(permissions));
        System.out.println();
    }
    
    // Permission bit values
    private static final long ORGANIZATION_UPDATE = 1L << 0;
    private static final long MEMBER_VIEW = 1L << 3;
    private static final long MEMBER_ADD = 1L << 4;
    private static final long MEMBER_REMOVE = 1L << 5;
    private static final long MEMBER_UPDATE_ROLE = 1L << 6;
    private static final long ROLE_VIEW = 1L << 7;
    private static final long ROLE_CREATE = 1L << 8;
    private static final long ROLE_UPDATE = 1L << 9;
    private static final long ROLE_DELETE = 1L << 10;
    private static final long ROLE_ASSIGN_PERMISSIONS = 1L << 11;
    private static final long PERMISSION_VIEW = 1L << 12;
    private static final long INVITATION_VIEW = 1L << 13;
    private static final long INVITATION_CREATE = 1L << 14;
    private static final long INVITATION_CANCEL = 1L << 15;
    private static final long USER_VIEW = 1L << 16;
    private static final long USER_UPDATE = 1L << 17;
    private static final long USER_DELETE = 1L << 18;
    private static final long DEPLOYMENT_CREATE = 1L << 19;
    private static final long DEPLOYMENT_VIEW = 1L << 20;
    private static final long DEPLOYMENT_ROLLBACK = 1L << 21;
    private static final long BILLING_VIEW = 1L << 22;
    private static final long BILLING_MANAGE = 1L << 23;
    private static final long MOBILE_APP_READ = 1L << 24;
    private static final long MOBILE_APP_CREATE = 1L << 25;
    private static final long MOBILE_APP_UPDATE = 1L << 26;
    private static final long MOBILE_APP_DELETE = 1L << 27;
    private static final long BUILD_VIEW = 1L << 28;
    private static final long BUILD_UPLOAD = 1L << 29;
    private static final long BUILD_DELETE = 1L << 30;
    
    private static long getOwnerPermissions() {
        // All permissions
        return ORGANIZATION_UPDATE | MEMBER_VIEW | MEMBER_ADD | MEMBER_REMOVE | MEMBER_UPDATE_ROLE |
               ROLE_VIEW | ROLE_CREATE | ROLE_UPDATE | ROLE_DELETE | ROLE_ASSIGN_PERMISSIONS |
               PERMISSION_VIEW | INVITATION_VIEW | INVITATION_CREATE | INVITATION_CANCEL |
               USER_VIEW | USER_UPDATE | USER_DELETE |
               DEPLOYMENT_CREATE | DEPLOYMENT_VIEW | DEPLOYMENT_ROLLBACK |
               BILLING_VIEW | BILLING_MANAGE |
               MOBILE_APP_READ | MOBILE_APP_CREATE | MOBILE_APP_UPDATE | MOBILE_APP_DELETE |
               BUILD_VIEW | BUILD_UPLOAD | BUILD_DELETE;
    }
    
    private static long getAdminPermissions() {
        // All permissions except BILLING_MANAGE
        return ORGANIZATION_UPDATE | MEMBER_VIEW | MEMBER_ADD | MEMBER_REMOVE | MEMBER_UPDATE_ROLE |
               ROLE_VIEW | ROLE_CREATE | ROLE_UPDATE | ROLE_DELETE | ROLE_ASSIGN_PERMISSIONS |
               PERMISSION_VIEW | INVITATION_VIEW | INVITATION_CREATE | INVITATION_CANCEL |
               USER_VIEW | USER_UPDATE | USER_DELETE |
               DEPLOYMENT_CREATE | DEPLOYMENT_VIEW | DEPLOYMENT_ROLLBACK |
               BILLING_VIEW |
               MOBILE_APP_READ | MOBILE_APP_CREATE | MOBILE_APP_UPDATE | MOBILE_APP_DELETE |
               BUILD_VIEW | BUILD_UPLOAD | BUILD_DELETE;
    }
    
    private static long getDeveloperPermissions() {
        return MEMBER_VIEW | ROLE_VIEW |
               DEPLOYMENT_CREATE | DEPLOYMENT_VIEW | DEPLOYMENT_ROLLBACK |
               USER_VIEW | INVITATION_VIEW |
               MOBILE_APP_READ | MOBILE_APP_CREATE | MOBILE_APP_UPDATE | MOBILE_APP_DELETE |
               BUILD_VIEW | BUILD_UPLOAD | BUILD_DELETE;
    }
    
    private static long getAnalystPermissions() {
        return MEMBER_VIEW | ROLE_VIEW | PERMISSION_VIEW |
               INVITATION_VIEW | USER_VIEW | USER_UPDATE |
               DEPLOYMENT_VIEW | BILLING_VIEW |
               MOBILE_APP_READ | BUILD_VIEW;
    }
    
    private static long getSupportPermissions() {
        return MEMBER_VIEW | INVITATION_VIEW | INVITATION_CREATE |
               USER_VIEW | USER_UPDATE | USER_DELETE |
               DEPLOYMENT_VIEW |
               MOBILE_APP_READ | BUILD_VIEW;
    }
}

