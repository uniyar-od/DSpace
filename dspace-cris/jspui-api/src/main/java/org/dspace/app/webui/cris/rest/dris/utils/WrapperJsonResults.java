package org.dspace.app.webui.cris.rest.dris.utils;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.dspace.app.webui.cris.rest.dris.JsonLdResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WrapperJsonResults<T extends JsonLdResult>
{
    private List<T> elements;
    private long totalElements;
    @JsonIgnore
    private Date lastModified;
    @JsonIgnore
    private Integer statusCode = HttpServletResponse.SC_OK;
    
    public List<T> getElements()
    {
        return elements;
    }
    public void setElements(List<T> elements)
    {
        this.elements = elements;
    }
    public long getTotalElements()
    {
        return totalElements;
    }
    public void setTotalElements(long totalElements)
    {
        this.totalElements = totalElements;
    }
    
    @JsonIgnore
    public boolean isEmpty()
    {
        if(elements!=null && !elements.isEmpty()) {
            return elements.isEmpty();
        }
        return true;
    }
    
    @JsonIgnore
    public int size()
    {
        if(elements!=null && !elements.isEmpty()) {
            return elements.size();
        }
        return 0;
    }
    public Object get(int i)
    {
        if(elements!=null && !elements.isEmpty()) {
            return elements.get(i);
        }
        return null;
    }
    public Date getLastModified()
    {
        return lastModified;
    }
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }
    public Integer getStatusCode()
    {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }
}
