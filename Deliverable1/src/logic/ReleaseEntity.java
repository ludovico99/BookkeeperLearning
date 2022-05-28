package logic;

import java.util.ArrayList;
import java.util.List;

public class ReleaseEntity {
    private String version;
    private String date;

    private final List<JavaFileEntity> javaFiles;

    public ReleaseEntity(String versionNumber, String versionDate)
    {
        version=versionNumber;
        date=versionDate;
        javaFiles = new ArrayList<>();

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<JavaFileEntity> getJavaFiles() {
        return javaFiles;
    }

    public void addJavaFile( JavaFileEntity javaFile){
        this.javaFiles.add(javaFile);
    }

    @Override
    public String toString (){
        return String.format("{%s,%s}",date,version);
    }
}
