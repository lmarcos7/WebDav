package platform.demo.dto;

import java.io.Serializable;

public class Breadcrumb implements Serializable
{

    private String name;
    private String relative;


    public Breadcrumb(final String name, final String relativePath){
        this.name = name;
        this.relative= relativePath;
    }

    public String getName() {
        return this.name;
    }

    public String getRelative() {
        return this.relative;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setRelative(final String relativePath) {
        this.relative = relativePath;
    }

}