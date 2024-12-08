package git2jar.project;

import java.util.Objects;

import git2jar.build.VersionNumber;

public class Tag {
    private String tag;
    private boolean built;
    private String sort;

    public Tag(String tag, boolean built) {
        setTag(tag);
        this.built = built;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        this.sort = new VersionNumber(tag).sort();
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        return Objects.equals(tag, other.tag);
    }

    public String sort() {
    	return sort;
    }
}
