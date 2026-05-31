package models;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Organization currentOrganization;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentOrganization(Organization organization) {
        this.currentOrganization = organization;
    }
    
    public Organization getCurrentOrganization() {
        return currentOrganization;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    public boolean isCoordinator() {
        return currentUser != null && currentUser.isCoordinator();
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    public int getCurrentOrganizationId() {
        if (currentUser != null && currentUser.isCoordinator()) {
            return currentUser.getOrganizationId();
        }
        if (currentOrganization != null) {
            return currentOrganization.getOrganizationId();
        }
        return -1;
    }
    
    public void clearSession() {
        currentUser = null;
        currentOrganization = null;
    }
}