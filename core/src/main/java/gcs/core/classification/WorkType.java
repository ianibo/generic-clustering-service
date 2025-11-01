package gcs.core.classification;

public enum WorkType {
    BOOK_MONOGRAPH("workType/book-monograph"),
    SERIAL("workType/serial"),
    THESIS("workType/thesis"),
    ARCHIVAL("workType/archival"),
    OBJECT("workType/object"),
    MAP("workType/map"),
    SOUND("workType/sound"),
    VIDEO("workType/video");

    private final String id;

    WorkType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
