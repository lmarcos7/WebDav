package platform.demo.dto;

import java.io.Serializable;


public class WebDAVRes implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;
    private String contentType;
    private String modifiedDate;
    private boolean file;
    private boolean directory;
    private String size;

    public WebDAVRes(final String name, final String contentType, final String modifiedDate, final boolean file, final boolean directory, final String size) {
        this.name = name;
        this.contentType = contentType;
        this.modifiedDate = modifiedDate;
        this.file = file;
        this.directory = directory;
        this.size = size;
    }

    public String getName() {
        return this.name;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getModifiedDate() {
        return this.modifiedDate;
    }

    public String getSize() {
        return this.size;
    }

    public boolean isFile() {
        return this.file;
    }

    public boolean isDirectory() {
        return this.directory;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setModifiedDate(final String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setFile(final boolean file) {
        this.file = file;
    }

    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }

    public void setSize(final String size) {
        this.size = size;
    }


}
