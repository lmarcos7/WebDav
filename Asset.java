package platform.demo.dto;

import java.io.Serializable;

public class Asset implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int syscode;
    private String code;
    private String description;
    private String brand;
    private String status;

    public Asset(final int aSyscode, final String aCode, final String aDescription, final String aBrand, final String aStatus) {
        this.syscode = aSyscode;
        this.code = aCode;
        this.description = aDescription;
        this.brand = aBrand;
        this.status = aStatus;
    }

    public int getSyscode() {
        return this.syscode;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getStatus() {
        return this.status;
    }

    public void setSyscode(final int aSyscode) {
        this.syscode = aSyscode;
    }

    public void setCode(final String aCode) {
        this.code = aCode;
    }

    public void setDescription(final String aDescription) {
        this.description = aDescription;
    }

    public void setBrand(final String aBrand) {
        this.brand = aBrand;
    }

    public void setStatus(final String aStatus) {
        this.status = aStatus;
    }
}
