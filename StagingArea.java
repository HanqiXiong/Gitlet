package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class StagingArea implements Serializable {

    /** The files added to the staging area. */
    private TreeMap<String, String> addedFiles;

    /** The file removed at the staging are. */
    private ArrayList<String> removeFiles;

    /** Initiate a staging area. */
    public StagingArea() {
        addedFiles = new TreeMap<>();
        removeFiles = new ArrayList<>();
    }

    /** Check if the added file exists.
     * @return if exists
     * @param name the name
     * */
    public boolean exists(String name) {
        if (addedFiles.isEmpty()) {
            return false;
        }
        for (String key : addedFiles.keySet()) {
            if (name.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /** Check if two files are the same.
     * @return if are the same
     * @param sha1 the sha1 code
     * */
    public boolean areSame(String sha1) {
        if (addedFiles.isEmpty()) {
            return false;
        }
        for (String values :addedFiles.values()) {
            if (sha1.equals(values)) {
                return true;
            }
        }
        return false;
    }

    /** Reset the stagingarea.*/
    public void reset() {
        addedFiles = new TreeMap<>();
        removeFiles = new ArrayList<>();
    }

    /** Get added files.
     * @return added files
     * */
    public TreeMap<String, String> getAddedFiles() {
        return addedFiles;
    }

    /** Get removed files.
     * @return removed files
     * */
    public ArrayList<String> getRemoveFiles() {
        return removeFiles;
    }
}
