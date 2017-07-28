package seth.oldNomenclature;

import java.util.regex.Pattern;

/**
 * Created by philippe on 7/10/17.
 */
class NomenclaturePattern {
    private final Pattern pattern;	//The Java regex pattern
    private final String regex;		//The pattern used to build the pattern
    private final int id;			//The identifier (line count) of the pattern

    public NomenclaturePattern(Pattern pattern, String regex, int id) {
        this.pattern = pattern;
        this.regex = regex;
        this.id = id;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getRegex() {
        return regex;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "NomenclaturePattern{" +
                "regex='" + regex + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NomenclaturePattern that = (NomenclaturePattern) o;

        if (id != that.id) return false;
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;
        return regex != null ? regex.equals(that.regex) : that.regex == null;
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        result = 31 * result + id;
        return result;
    }
}
