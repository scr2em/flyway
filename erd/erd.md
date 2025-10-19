erDiagram
    users ||--o{ refresh_tokens : "has"
    users ||--o{ organizations : "creates"
    users ||--o{ organization_members : "belongs to"
    users ||--o{ invitations : "invites"
    users ||--|| user_statuses : "has status"
    users ||--o{ mobile_applications : "creates"
    users ||--o{ app_builds : "uploads"
    users ||--o{ api_keys : "creates"
    
    organizations ||--o{ organization_members : "has"
    organizations ||--o{ invitations : "sends"
    organizations ||--o{ mobile_applications : "owns"
    organizations ||--o{ app_builds : "has"
    organizations ||--o{ api_keys : "manages"
    organizations ||--o{ channels : "has"
    
    mobile_applications ||--o{ app_builds : "has builds"
    mobile_applications ||--o{ api_keys : "has keys"
    
    roles ||--o{ organization_members : "assigned to"
    roles ||--o{ invitations : "offered in"
    
    invitations ||--|| invitation_statuses : "has status"
    
    users {
        uuid id PK
        string first_name
        string last_name
        string email UK "unique"
        string password_hash
        uuid user_status_id FK
        boolean email_verified
        timestamp email_verified_at
        timestamp last_login_at
        timestamp created_at
        timestamp updated_at
    }
    
    user_statuses {
        uuid id PK
        string code UK "active, suspended, deleted"
        string label "Active, Suspended, Deleted"
        timestamp created_at
    }
    
    refresh_tokens {
        uuid id PK
        uuid user_id FK
        string token_hash UK "unique, hashed"
        boolean is_revoked
        timestamp revoked_at
        string device_info "optional"
        string ip_address "optional"
        timestamp expires_at
        timestamp created_at
    }
    
    organizations {
        uuid id PK
        string name
        string subdomain UK "unique, optional"
        uuid created_by FK "user who created"
        timestamp created_at
        timestamp updated_at
    }
    
    organization_members {
        uuid id PK
        uuid organization_id FK
        uuid user_id FK
        uuid role_id FK "required"
        timestamp joined_at
        timestamp created_at
    }
    
    roles {
        uuid id PK
        string name UK "unique"
        text description "optional"
        bigint permissions "bitwise permissions mask"
        timestamp created_at
        timestamp updated_at
    }
    
    invitations {
        uuid id PK
        uuid organization_id FK
        string email
        uuid role_id FK
        uuid invited_by FK
        uuid invitation_status_id FK
        string token UK "unique invitation token"
        timestamp expires_at
        timestamp responded_at
        timestamp created_at
        timestamp updated_at
    }
    
    invitation_statuses {
        uuid id PK
        string code UK "pending, accepted, rejected, expired"
        string label "Pending, Accepted, Rejected, Expired"
        timestamp created_at
    }
    
    mobile_applications {
        uuid id PK
        string bundle_id UK "unique"
        uuid organization_id FK
        string name
        text description "optional"
        uuid created_by FK
        timestamp created_at
        timestamp updated_at
    }
    
    app_builds {
        uuid id UK "unique"
        uuid organization_id FK
        string bundle_id FK
        string commit_hash
        string branch_name
        text commit_message "optional"
        bigint build_size
        string build_url
        string native_version
        uuid uploaded_by FK
        timestamp created_at
        timestamp updated_at
        string composite_pk "PK(organization_id, bundle_id, commit_hash)"
    }
    
    api_keys {
        uuid id PK
        string key_hash UK "unique, hashed"
        string key_prefix
        string name
        string bundle_id FK
        uuid organization_id FK
        uuid created_by FK
        timestamp last_used_at "optional"
        timestamp expires_at "optional"
        timestamp created_at
        timestamp updated_at
    }
    
    channels {
        uuid id PK
        string name
        text description "optional"
        uuid organization_id FK
        timestamp created_at
        timestamp updated_at
        string composite_uk "UK(name, organization_id)"
    }
    
    audit_logs {
        uuid id PK
        uuid user_id "not FK, for reference only"
        uuid organization_id "not FK, for reference only"
        string action
        string resource_type "optional"
        uuid resource_id "optional"
        string resource_name "optional"
        string http_method "optional"
        string endpoint "optional"
        string ip_address "optional"
        text user_agent "optional"
        text request_body "optional"
        int response_status "optional"
        text error_message "optional"
        json metadata "optional"
        timestamp created_at
    }