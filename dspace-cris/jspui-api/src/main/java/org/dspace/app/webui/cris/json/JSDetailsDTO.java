package org.dspace.app.webui.cris.json;

import java.util.List;

/**
 * 
 * Class to provide details structure to fill in the JSON
 * 
 * @author Luigi Andrea Pascarelli
 *
 */
public class JSDetailsDTO
{
    private String label;
    private List<String> value;
    private boolean url;
    
    public String getLabel()
    {
        return label;
    }
    public void setLabel(String label)
    {
        this.label = label;
    }
    public List<String> getValue()
    {
        return value;
    }
    public void setValue(List<String> value)
    {
        this.value = value;
    }
    public boolean isUrl()
    {
        return url;
    }
    public void setUrl(boolean url)
    {
        this.url = url;
    }
    
}
