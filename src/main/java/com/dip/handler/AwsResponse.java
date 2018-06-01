package com.dip.handler;

import java.io.Serializable;

public class AwsResponse implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 1039210558343572679L;

    /** longUrl */
    private String longUrl;

    /** remark */
    private String remark;

    /** shortUrl */
    private String shortUrl;

    @Override
    public AwsResponse clone() {
        try {
            return (AwsResponse) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof AwsResponse == false) {
            return false;
        }
        final AwsResponse other = (AwsResponse) obj;
        if (other.getLongUrl() == null ^ getLongUrl() == null) {
            return false;
        }
        if (other.getLongUrl() != null && other.getLongUrl().equals(getLongUrl()) == false) {
            return false;
        }
        if (other.getRemark() == null ^ getRemark() == null) {
            return false;
        }
        if (other.getRemark() != null && other.getRemark().equals(getRemark()) == false) {
            return false;
        }
        if (other.getShortUrl() == null ^ getShortUrl() == null) {
            return false;
        }
        if (other.getShortUrl() != null && other.getShortUrl().equals(getShortUrl()) == false) {
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    public String getLongUrl() {
        return longUrl;
    }

    /**
     * @return
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @return
     */
    public String getShortUrl() {
        return shortUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + (getLongUrl() == null ? 0 : getLongUrl().hashCode());
        hashCode = prime * hashCode + (getRemark() == null ? 0 : getRemark().hashCode());
        hashCode = prime * hashCode + (getShortUrl() == null ? 0 : getShortUrl().hashCode());
        return hashCode;
    }

    /**
     * @param longUrl
     * @return Returns a reference to this object so that method calls can be chained together.
     */
    public AwsResponse longUrl(final String longUrl) {
        setLongUrl(longUrl);
        return this;
    }

    /**
     * @param remark
     * @return Returns a reference to this object so that method calls can be chained together.
     */
    public AwsResponse remark(final String remark) {
        setRemark(remark);
        return this;
    }

    /**
     * @param longUrl
     */
    public void setLongUrl(final String longUrl) {
        this.longUrl = longUrl;
    }

    /**
     * @param remark
     */
    public void setRemark(final String remark) {
        this.remark = remark;
    }

    /**
     * @param shortUrl
     */
    public void setShortUrl(final String shortUrl) {
        this.shortUrl = shortUrl;
    }

    /**
     * @param shortUrl
     * @return Returns a reference to this object so that method calls can be chained together.
     */
    public AwsResponse shortUrl(final String shortUrl) {
        setShortUrl(shortUrl);
        return this;
    }

    /**
     * Returns a string representation of this object; useful for testing and debugging.
     *
     * @return A string representation of this object.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getLongUrl() != null) {
            sb.append("longUrl: ").append(getLongUrl()).append(",");
        }
        if (getRemark() != null) {
            sb.append("remark: ").append(getRemark()).append(",");
        }
        if (getShortUrl() != null) {
            sb.append("shortUrl: ").append(getShortUrl());
        }
        sb.append("}");
        return sb.toString();
    }
}
