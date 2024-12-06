package git2jar.project;

import java.util.Objects;

public class Tag implements Comparable<Tag> {
    private String tag;
    private boolean built;

    public Tag() {
        this("", false);
    }

    public Tag(String tag, boolean built) {
        this.tag = tag;
        this.built = built;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    @Override
    public int compareTo(Tag o) {
        return o.tag.compareTo(tag);
    }
}
