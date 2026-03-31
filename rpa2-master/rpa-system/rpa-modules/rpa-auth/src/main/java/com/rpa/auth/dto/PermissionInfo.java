package com.rpa.auth.dto;

public class PermissionInfo {
    private Long id;
    private String permName;
    private String permKey;
    private Integer permType;

    public PermissionInfo() {}

    public PermissionInfo(Long id, String permName, String permKey, Integer permType) {
        this.id = id;
        this.permName = permName;
        this.permKey = permKey;
        this.permType = permType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }

    public String getPermKey() { return permKey; }
    public void setPermKey(String permKey) { this.permKey = permKey; }

    public Integer getPermType() { return permType; }
    public void setPermType(Integer permType) { this.permType = permType; }
}
