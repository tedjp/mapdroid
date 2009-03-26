package au.id.tedp.routed;

public class RoutePoint extends Object {
    private String desc;

    public String getDescription() {
        return desc;
    }

    public void setDescription(String newDesc) {
        desc = newDesc;
    }

    public String toString() {
	return desc;
    }
}
