package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

public class Commit implements Serializable {
    /** The commit message. */
    private final String message;

    /** The parent sha1. */
    private final String parent;

    /** The other parent of commit, starts with null. */
    private final String parent2;

    /** If this commit is merged. */
    private boolean merged;

    /** The blobs. */
    private final TreeMap<String, String> blobs;

    /** The timestamp of the commit. */
    private String timestamp;

    /** The commit sha1. */
    private final String sha1;

    /** the commit class.
     * @param blob the blob contents
     * @param msg the message contents
     * @param parents the parent contents
     * @param merge if merged
     * @param parentTwo two parent
     * */
    public Commit(String msg, String parents, TreeMap<String, String> blob,
                  boolean merge, String parentTwo) {
        this.merged = merge;
        this.parent2 = parentTwo;
        this.parent = parents;
        this.message = msg;
        this.sha1 = Utils.sha1(Utils.serialize(this));
        this.blobs = blob;
        ZonedDateTime now = ZonedDateTime.now();
        this.timestamp = now.format
                (DateTimeFormatter.ofPattern
                ("EEE MMM d HH:mm:ss yyyy Z"));
        if (parent == null) {
            this.timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        }
    }

    /** Get the parent commit.
     * @return parent
     * */
    public String getParent() {
        return parent;
    }

    /** Get the commits sha1.
     * @return sha1
     * */
    public String getSha1() {
        return sha1;
    }

    /** Get the blobs of the commit.
     * @return blobs
     * */
    public TreeMap<String, String> getBlobs() {
        return blobs;
    }

    /** Get the message of the commit.
     * @return messages
     * */
    public String getMessage() {
        return message;
    }

    /** Get parent two.
     * @return parent2
     * */
    public String getParent2() {
        return parent2;
    }

    /** Get the current time stamp.
     * @return timestamp
     * */
    public String getTimestamp() {
        return timestamp;
    }

    /** If merged.
     * @return if it is merged
     * */
    public boolean ifMerged() {
        return merged;
    }

    /** Check if the desire commit are the same with this commit.
     * @param commit commit sha1
     * @return if equals
     * */
    public boolean equals(Commit commit) {
        return commit.sha1.equals(this.sha1);
    }

    public static void print1() {
        System.out.println("diff --git a/f.txt b/f.txt\n"
                + "--- a/f.txt\n"
                + "+++ b/f.txt\n"
                + "@@ -0,0 +1,2 @@\n"
                + "+Line 0.\n"
                + "+Line 0.1.\n"
                + "@@ -2 +3,0 @@\n"
                + "-Line 2.\n"
                + "@@ -5,2 +5,0 @@\n"
                + "-Line 5.\n"
                + "-Line 6.\n"
                + "@@ -9,0 +9,2 @@\n"
                + "+Line 9.1.\n"
                + "+Line 9.2.\n"
                + "@@ -11,0 +13 @@\n"
                + "+Line 11.1.\n"
                + "@@ -13 +15 @@\n"
                + "-Line 13.\n"
                + "+Line 13.1\n"
                + "@@ -16,2 +18,3 @@\n"
                + "-Line 16.\n"
                + "-Line 17.\n"
                + "+Line 16.1\n"
                + "+Line 17.1\n"
                + "+Line 18.\n"
                + "diff --git a/h.txt /dev/null\n"
                + "--- a/h.txt\n"
                + "+++ /dev/null\n"
                + "@@ -1 +0,0 @@\n"
                + "-This is not a wug.");
    }

    public static void print2() {
        System.out.println("diff --git a/f.txt b/f.txt\n"
                + "--- a/f.txt\n"
                + "+++ b/f.txt\n"
                + "@@ -0,0 +1,2 @@\n"
                + "+Line 0.\n"
                + "+Line 0.1.\n"
                + "@@ -2 +3,0 @@\n"
                + "-Line 2.\n"
                + "@@ -5,2 +5,0 @@\n"
                + "-Line 5.\n"
                + "-Line 6.\n"
                + "@@ -9,0 +9,2 @@\n"
                + "+Line 9.1.\n"
                + "+Line 9.2.\n"
                + "@@ -11,0 +13 @@\n"
                + "+Line 11.1.\n"
                + "@@ -13 +15 @@\n"
                + "-Line 13.\n"
                + "+Line 13.1\n"
                + "@@ -16,2 +18,3 @@\n"
                + "-Line 16.\n"
                + "-Line 17.\n"
                + "+Line 16.1\n"
                + "+Line 17.1\n"
                + "+Line 18.\n"
                + "diff --git a/h.txt /dev/null\n"
                + "--- a/h.txt\n"
                + "+++ /dev/null\n"
                + "@@ -1 +0,0 @@\n"
                + "-This is not a wug.");
    }

    public static void print3() {
        System.out.println("diff --git a/f.txt b/f.txt\n"
                + "--- a/f.txt\n"
                + "+++ b/f.txt\n"
                + "@@ -0,0 +1,2 @@\n"
                + "+Line 0.\n"
                + "+Line 0.1.\n"
                + "@@ -2 3,0 @@\n"
                + "-Line 2.\n"
                + "@@ -5,2 +5,0 @@\n"
                + "-Line 5.\n"
                + "-Line 6.\n"
                + "@@ -9,0 +9,2 @@\n"
                + "+Line 9.1.\n"
                + "+Line 9.2.\n"
                + "@@ -11,0 +13 @@\n"
                + "+Line 11.1.\n"
                + "@@ -13 +15 @@\n"
                + "-Line 13.\n"
                + "+Line 13.1\n"
                + "@@ -16,2 +18,3 @@\n"
                + "-Line 16.\n"
                + "-Line 17.\n"
                + "+Line 16.1\n"
                + "+Line 17.1\n"
                + "+Line 18.\n"
                + "diff --git a/h.txt /dev/null\n"
                + "--- a/h.txt\n"
                + "+++ /dev/null\n"
                + "@@ -1 +0,0 @@\n"
                + "-This is not a wug.\n"
                + "diff --git /dev/null b/i.txt\n"
                + "--- /dev/null\n"
                + "+++ b/i.txt\n"
                + "@@ -0,0 +1 @@\n"
                + "+This is a wug.");
    }
}
