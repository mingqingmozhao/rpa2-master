package com.rpa.auth.dto;

import com.rpa.auth.model.Role;
import java.util.List;

/**
 * 角色列表分页响应 DTO（兼容前端 el-pagination）。
 */
public class RolePageResponse {

    private List<Role> records;
    private long total;
    private int page;
    private int pageSize;

    public RolePageResponse() {}

    public RolePageResponse(List<Role> records, long total, int page, int pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<Role> getRecords() { return records; }
    public void setRecords(List<Role> records) { this.records = records; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
