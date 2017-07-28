package seth.seth.eval;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 14.12.12
 * Time: 10:13
 * General representation of a textual mutation mention used for evaluation
 */
class Entity {
    private final String id;   // e.g. T1
    private final String tool; //SETH or MF

    private final int start;   // e.g. 66
    private final int end;     // e.g. 71
    private final String type; // e.g. SNP
    private final String entity; // e.g. G145V

    public Entity(String id, String type, int start, int end, String entity, String tool) {
        this.id = id;
        this.type = type;
        this.start = start;
        this.end = end;
        this.entity = entity;
        this.tool = tool;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", entity='" + entity + '\'' +
                ", tool=" +tool + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getEntity() {
        return entity;
    }

    public String getTool() {
        return tool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity1 = (Entity) o;

        if (end != entity1.end) return false;
        if (start != entity1.start) return false;
        if (entity != null ? !entity.equals(entity1.entity) : entity1.entity != null) return false;
        if (type != null ? !type.equals(entity1.type) : entity1.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}
