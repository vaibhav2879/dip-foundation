package com.dip.handler;

import java.io.Serializable;

public class AwsRequest implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 9150936836766760147L;

    /** the url */
    private String url;

    /** the domain name */
    private String domainName;

    @Override
    public AwsRequest clone() {
        try {
            return (AwsRequest) super.clone();
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
        if (obj instanceof AwsRequest == false) {
            return false;
        }
        final AwsRequest other = (AwsRequest) obj;
        if (other.getUrl() == null ^ getUrl() == null) {
            return false;
        }
        if (other.getUrl() != null && other.getUrl().equals(getUrl()) == false) {
            return false;
        }
        if (other.getDomainName() == null ^ getDomainName() == null) {
            return false;
        }
        if (other.getDomainName() != null && other.getDomainName().equals(getDomainName()) == false) {
            return false;
        }
        return true;
    }

    /**
     * @param domainName
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * @return
     */
    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + (getUrl() == null ? 0 : getUrl().hashCode()) + (getDomainName() == null ? 0 : getDomainName().hashCode());
        return hashCode;
    }

    /**
     * @return
     */
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * @param url
     */
    public void setUrl(final String url) {
        this.url = url;
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
        if (getUrl() != null) {
            sb.append("url: ").append(getUrl());
        }
        if (getUrl() != null && getDomainName() != null) {
            sb.append(",");
        }
        if (getDomainName() != null) {
            sb.append("domainName: ").append(getDomainName());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * @param url
     * @return Returns a reference to this object so that method calls can be chained together.
     */
    public AwsRequest url(final String url, final String domainName) {
        setUrl(url);
        setDomainName(domainName);
        return this;
    }
}
